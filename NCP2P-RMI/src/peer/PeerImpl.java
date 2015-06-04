package peer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import com.Configuration;
import com.DigestCalc;
import com.FileInfo;
import com.Peer;
import com.SuperPeer;
import com.TwoWayHashMap;

public class PeerImpl implements Peer {

	private Configuration conf;
	public String nick;
	public View view;
	
	private SuperPeerStub sp;
	//Thread spThrd;
	
	public File shareDir;
	private WatchDir watcher;
	
	private DownloadManager dm;
	
	private TwoWayHashMap<String,File> files; //mapping from filename to md5 and vice versa
	private Vector<File> ignored;

	
	public PeerImpl(View v, String confFile){
		
		this.view=v;
		this.conf=Configuration.getConf(confFile); //load Configuration from xml file
		
		files=new TwoWayHashMap<String,File>();
		ignored=new Vector<File>();
		
		nick=conf.getNick();
		if(nick.trim().equalsIgnoreCase("")){
			nick="Peer" + new Random().nextInt(10);
		}
		
		shareDir=new File(conf.getSharedDir());
		sp=new SuperPeerStub(this,conf.getSuperpeers()); //establishes connection and register nickname
		sp.initConnection();
		System.out.println("sp created");
		
		view.setInfo("SHARE", conf.getSharedDir());
		dm=new DownloadManager(this,conf.getStuns());

	}
	
	void fileChanged(File f,int stat){
		String strfi=null;
		DigestCalc d=new DigestCalc();

		if(ignored.contains(f)){ //file is being downloaded
			return;
		}
		if(stat==FileInfo.DELETE){
			strfi = files.getBackward((File)f);
			files.remove(strfi);
			System.out.println("DELETED:" + f.getName());
		}else if(stat==FileInfo.CREATE){
			strfi=d.calculateMD5(f);
			files.put(strfi,(File)f);
			System.out.println("CREATED:" + f.getName());
		}else if(stat==FileInfo.MODIFY){
			strfi=files.getBackward((File)f); //old mapping
			files.remove(strfi);			//remove old mapping
			strfi=d.calculateMD5(f);
			files.put(strfi,(File)f);	//add new mapping
			System.out.println("MODIFIED:" + f.getName());
		}

			try{
				sp.fileChanged(nick,f.getName(),f.length(),strfi,stat);
				//TODO: rather than sending FileInfo object, send only nameOf file, Size in bytes, Checksum.. enough to construct fi at server side.
			}catch(NotConnectedException ne){
				//Do nothing
			}
	}
	
	void downloadFile(String checksum,String localName){
		
		File f=files.getForward(checksum);
		if(f!=null){
			view.showMessage("You already have this file saved as "+ f.getName());
			System.out.println("You already have this file saved as "+ f.getName());
			return;
		}
		try{
			
			FileInfo fi=sp.getFileInfo(checksum);
			if(fi==null){
				System.out.println("File not available!");
				view.showMessage("File not available!");
				return;
			}
			
			int sessionID=new Random(System.currentTimeMillis()).nextInt(255);
			
			f=new File(shareDir + "\\" +  localName);
			ignored.add(f);
			
			dm.addDownload(fi, f, sessionID); //local representative of this download
	
			boolean b=sp.downloadFile(dm.getExternalAddress(),checksum,sessionID);
			if(b==true){
				System.out.println("SuperPeer initiated download");
			}else{
				System.out.println("SuperPeer failed to initiate download");
				view.showMessage("SuperPeer failed to initiate download");
				dm.removeDownload(sessionID);
			}
		}catch(NotConnectedException ne){ view.showMessage("Not connected!"); }
	}
	
	String[][] searchFile(String query){

		try{
			HashSet<FileInfo> searchResult=sp.searchFile(query);
			int resCnt=searchResult.size();
			String[][] strRes=new String[resCnt][5];
			int i=0;
			
			Iterator<FileInfo> iter = searchResult.iterator();
			while(iter.hasNext()){
				 	FileInfo fi = iter.next();
				 	//name tags checksum length, seeders
					System.out.format("%25s\t%10dKB\t%16s\t",fi.toString(), fi.getLen(), fi.getChecksum());
					strRes[i][0]=fi.toString();
					strRes[i][2]="" + fi.getLen()/1024;
					strRes[i][4]=fi.getChecksum();
					
					
					Iterator<String> tit=fi.getTags().iterator();
					while(tit.hasNext()){
						String s=tit.next();
						System.out.format("%s,",s);
						strRes[i][1]=s + ", ";
					}
					
					System.out.format("\t");
					Iterator<String> pit=fi.getSeeders().iterator();
					while(pit.hasNext()){
						String p=pit.next();
						System.out.format("%s,",p);
						strRes[i][3]=p + ", ";
					}
					i++;
					System.out.println();
			}
			return strRes;
		}catch(NotConnectedException ne){
			view.showMessage("Not Connected!");
			return null;
		}
	}
	
	//****************TO BE CALLED by SERVER **************
	@Override
	public boolean uploadBlock(String strfi,int blkfrm,int blkto,InetSocketAddress dest,int sessionID) {
		File f=files.getForward(strfi);
		if(f==null){
			return false;
		}
		
		Upload u=dm.addUpload(f,strfi,blkfrm,blkto, sessionID,dest);
		if(u==null){
			return false;
		}
		
		dm.startUpload(sessionID);							//This should be point of fork, so that immediate response can be returned to server
		System.out.println("UPLOAD:" + strfi + " : " + blkfrm + "-->"+ blkto + " to " + dest);
		
		return true;
	}
	
	@Override
	public String toString(){
		return nick;
	}

	public void downloadComplete(File f) {
		boolean b=ignored.remove(f);
		System.out.println("UNIGNORE:" + b + " Download Complete " + f.getName());
		view.showMessage("Download complete :" + f.getName());
		fileChanged(f,FileInfo.CREATE);
	}
	
	public Configuration getConf(){
		return conf;
	}

	public void sendAllFilesInfo() {	//Inform file metadata to (new) sp
		try {
			if(watcher==null){
				watcher=new WatchDir(shareDir.toPath(),false,this);
				Thread watcherThread=new Thread(watcher);
				watcherThread.start();
				System.out.println("Started watch Service");
			}
			System.out.println("Reading current files");
			watcher.readCurrentFiles(shareDir);
			
			
		}catch(IOException ex){
			System.out.println("Unable to start directory watch Service");
		}
	}
}//class
