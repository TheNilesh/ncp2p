package speer;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import com.FileInfo;
import com.Peer;
import com.SuperPeer;

public class SuperPeerImpl implements SuperPeer {

	private transient Hashtable<String,Peer> peers;
	private transient Hashtable<String,FileInfo> files;
	private SPServer server;
	
	public static void main(String args[]){
		try{
			new SuperPeerImpl();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public SuperPeerImpl() throws IOException{
		peers=new Hashtable<String,Peer>();
		files=new Hashtable<String,FileInfo>();
		server=new SPServer(this,4012);
	}
	
	@Override
	public boolean fileChanged(String p, String fileName, long fileSize, String checksum, int stat) {
		System.out.println("SP.fileChanged()");
		boolean response=false;
		FileInfo fi;
		if(stat==FileInfo.CREATE){
			if(files.containsKey(checksum)){  //already Available
				fi=files.get(checksum);
				fi.addSeeder(p);
				response=true;
			}else{
				fi=new FileInfo(fileName,fileSize,checksum); //new Unique
				fi.addSeeder(p);
				files.put(checksum, fi);
				response=true;
			}
		}else if(stat==FileInfo.DELETE){ 
			fi=files.get(checksum);
			if(fi!=null){					//delete already avail
				fi.removeSeeder(p);
				if(fi.seederCount()<1){ //No seeder so remove file
					files.remove(checksum);
				}
				response=true;
			}else{							//delete already avail
				System.out.println(fi+ " is NOT on server to DELETE");
			}
		}
		return response;
	}


	@Override
	public boolean downloadFile(SocketAddress dest,String checksum,int sessionID) {
		System.out.println("SP.downloadFile()");
		if(!files.containsKey(checksum)){
			return false;
		}
		boolean success=false;
		FileInfo fi=files.get(checksum);
		HashSet<String> seeders=fi.getSeeders();
		
		int scnt=seeders.size();	//Source count
		int blkcnt=fi.getBlocksCount();//Block count
		System.out.println("BLOCK COUNT: " + blkcnt);
		int eqJ=blkcnt/scnt;	//equal Job
		int exJ=blkcnt % eqJ;	//extra Job
		
		int blkfrm=0;	//from this block
		int blkto;		//to this block
		for(String s:seeders){
			blkto=blkfrm + eqJ;
			if(exJ>0){
				exJ--;
				blkto++;
			}
			
			//ask s to send Blocks from blkfrm to blkto
			Peer p=peers.get(s);
			if(p==null){ //worst case, if peer unregistered
				
			}else{
				p.uploadBlock(fi.getChecksum(),blkfrm,blkto,dest);
				System.out.println(p.toString() + "--> FROM:" + blkfrm + " TO:" + blkto);
			}
			
			blkfrm=blkto;
		}
		
		return success;
	}

	@Override
	public boolean register(Peer p,boolean status) {
		System.out.println("SP.register() : " + status);
		// add Peer into peerList
		boolean success=false;
		if(status){
			success=( peers.put(p.toString(), p) == null); /*//Returns:the previous value of the specified key in this hashtable, or null if it did not have one
															*If adding peer succeed? two peers with same name cant be added*/
		}else{
			success=(peers.remove(p.toString()) != null);
			//Peer unregistred, delete its file from files
			for(FileInfo fi:files.values()){
				fi.removeSeeder(p.toString());
				if(fi.seederCount()<1){ //No seeder so remove file
					files.remove(fi.getChecksum());
				}
			}
			
		}
		return success;
	}

	@Override
	public HashSet<FileInfo> searchFile(String query) {
		System.out.println("SP.searchFile()");
		/*Iterate through files for mtching tags
		 * remove file to which seeder is p, /*because he already have that file
		 * return result;
		 */
		FileInfo fi;
		HashSet<FileInfo> match=new HashSet<FileInfo>();
		Collection<FileInfo> files2=files.values();
		Iterator<FileInfo> it=files2.iterator();
		while(it.hasNext()){
			fi=it.next();
			if(fi.tagMatches(query)){
				//System.out.println(fi);
				match.add(fi);
			}
		}
		return match;
	}

}
