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
		
		nick="PeerN" + new Random().nextInt(10);
		sp=new SuperPeerStub(this,"localhost",4012);
		
		spThrd=new Thread((Runnable) sp); //? Runnable
		spThrd.start();
		while(!sp.register(this, true)){ //registration failed, then go inside loop, to change nickname
			nick="Peer" + new Random().nextInt(20);
		};
		
		
		try{
			shareDir=new File("E:\\TEST3");
			watcher=new WatchDir(shareDir.toPath(),false,this);
			Thread watcherThread=new Thread(watcher);
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
			try {
				strfi=d.calculateMD5(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
			files.put(strfi,(File)f);
		}else if(stat==FileInfo.MODIFY){
			strfi=files.getBackward((File)f); //old mapping
			files.remove(strfi);			//remove old mapping
			try {
				strfi=d.calculateMD5(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
			files.put(strfi,(File)f);	//add new mapping
		}

		System.out.println("CHANGE:" + f.getName());
		
		sp.fileChanged(nick,f.getName(),f.length(),strfi,stat);
		//TODO: rather than sending FileInfo object, send only nameOf file, Size in bytes, Checksum.. enough to construct fi at server side.
	}
	
	void downloadFile(String checksum,String localName){
		FileInfo fi=sp.getFileInfo(checksum);
		if(fi==null){
			System.out.println("File not available!");
			return;
		}
		int dport=dm.getPort();
		String dest=":" + dport; //superpeer will fill first part
		int sessionID=new Random().nextInt();

		File f=new File(shareDir + "\\" +  localName);
		if(dport!=0 && dport!=-1){
			dm.addDownload(fi, f, sessionID); //local representative of this download
			sp.downloadFile(dest,checksum,sessionID);
			//TODO: fix this situation without adding more remote Calls
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
	public boolean uploadBlock(String strfi,int blkfrm,int blkto,String dest) {
		// This function will be called by remote server
	//	FileInfo fi=files.get(strfi);
	//	if(fi==null){
	//		return false;
	//	}
	//	//somehow get File from checksum recvd, and then give it to upload
	//	boolean b=dm.addUpload(fi, f, sessionID);
		System.out.println("UPLOADing:" + strfi + " : " + blkfrm + "-->"+ blkto + " to " + dest);
		
		return false;
	}
	
	@Override
	public String toString(){
		return nick;
	}
}
