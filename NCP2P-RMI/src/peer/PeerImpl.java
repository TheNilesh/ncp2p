package peer;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import com.DigestCalc;
import com.FileInfo;
import com.Peer;
import com.SuperPeer;
import com.TwoWayHashMap;

public class PeerImpl implements Peer {

	public String nick;
	
	SuperPeer sp;
	Thread spThrd;
	
	public File shareDir;
	private WatchDir watcher;
	private Thread watcherThread;
	
	private DownloadManager dm;
	
	private TwoWayHashMap<String,File> files; //mapping from filename to md5
	
	public PeerImpl(){
		
		files=new TwoWayHashMap<String,File>();
		
		nick="Peer" + new Random().nextInt(10);
		sp=new SuperPeerStub(this,"localhost",4012);
		
		spThrd=new Thread((Runnable) sp); //? Runnable
		spThrd.start();
		while(!sp.register(this, true)){ //registration failed, then go inside loop, to change nickname
			nick="Peer" + new Random().nextInt(20);
		};
		
		
		try{
			shareDir=new File("E:\\TEST1");
			watcher=new WatchDir(shareDir.toPath(),false,this);
			Thread watcherThread=new Thread(watcher);
			watcherThread.start();
		}catch(IOException ex){
			System.out.println("Unable to start file watch Service");
		}
		
		int udpport=5012;
		while(true){
			try {
				dm=new DownloadManager(this,udpport);
				break;							 //no exception
			} catch (SocketException e) {udpport++;}
		}//while loop
	}
	
	void fileChanged(File f,int stat){
		String strfi=null;
		DigestCalc d=new DigestCalc();

		if(stat==FileInfo.DELETE){
			strfi = files.getBackward((File)f);
			files.remove(strfi);
		}else if(stat==FileInfo.CREATE){
			strfi=d.calculateMD5(f);
			files.put(strfi,(File)f);
		}else if(stat==FileInfo.MODIFY){
			strfi=files.getBackward((File)f); //old mapping
			files.remove(strfi);			//remove old mapping
			strfi=d.calculateMD5(f);
			files.put(strfi,(File)f);	//add new mapping
		}

		System.out.println("CHANGE:" + f.getName());
		
		sp.fileChanged(nick,f.getName(),f.length(),strfi,stat);
		//TODO: rather than sending FileInfo object, send only nameOf file, Size in bytes, Checksum.. enough to construct fi at server side.
	}
	
	void downloadFile(String checksum,String localName){
		
		File f=files.getForward(checksum);
		if(f!=null){
			System.out.println("You already have this file saved as "+ f.getName());
			return;
		}
		
		FileInfo fi=sp.getFileInfo(checksum);
		if(fi==null){
			System.out.println("File not available!");
			return;
		}
		int dport=dm.getPort();
		String dest=":" + dport; //superpeer will fill first part
		int sessionID=new Random().nextInt();

		f=new File(shareDir + "\\" +  localName);
		if(dport!=0 && dport!=-1){
			dm.addDownload(fi, f, sessionID); //local representative of this download
			boolean b=sp.downloadFile(dest,checksum,sessionID);
			if(b==true){
				System.out.println("Server initiated download");
			}else{
				System.out.println("Server failed to initiate download");
			}
		}else{
			System.out.println("Download failed, local endpoint is not ready!");
		}
	}
	
	void searchFile(String query){

		HashSet<FileInfo> searchResult=sp.searchFile(query);
		
		Iterator<FileInfo> iter = searchResult.iterator();
		while(iter.hasNext()){
			 	FileInfo fi = iter.next();
			 	//name tags checksum length, seeders
				System.out.format("%25s\t%10dKB\t%16s\t",fi.toString(), fi.getLen(), fi.getChecksum());
				Iterator<String> tit=fi.getTags().iterator();
				while(tit.hasNext()){
					String s=tit.next();
					System.out.format("%s,",s);
				}
				System.out.format("\t");
				Iterator<String> pit=fi.getSeeders().iterator();
				while(pit.hasNext()){
					String p=pit.next();
					System.out.format("%s,",p);
				}
				System.out.println();
				
		}
		
	}
	
	//****************TO BE CALLED by SERVER **************
	@Override
	public boolean uploadBlock(String strfi,int blkfrm,int blkto,String dest,int sessionID) {
		File f=files.getForward(strfi);
		if(f==null){
			return false;
		}
		
		Upload u=dm.addUpload(f,strfi,blkfrm,blkto, sessionID);
		if(u==null){
			return false;
		}
		
		dm.startUpload(sessionID);							//This should be point of fork, so that immediate response can be returned to server
		System.out.println("UPLOADing:" + strfi + " : " + blkfrm + "-->"+ blkto + " to " + dest);
		
		return true;
	}
	
	@Override
	public String toString(){
		return nick;
	}
}
