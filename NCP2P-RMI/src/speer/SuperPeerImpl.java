package speer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.FileInfo;
import com.Peer;
import com.SuperPeer;

public class SuperPeerImpl implements SuperPeer {

	private transient Hashtable<String,Peer> peers;
	private transient ConcurrentHashMap<String,FileInfo> files;
	private SPServer server;
	private STUNServer stun;
	
	public static void main(String args[]){
		try{
			new SuperPeerImpl();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public SuperPeerImpl() throws IOException{
		
		peers=new Hashtable<String,Peer>();
		files=new ConcurrentHashMap<String,FileInfo>();
		server=new SPServer(this,4012);
		stun=new STUNServer(5478);
	}
	
	@Override
	public boolean fileChanged(String p, String fileName, long fileSize, String checksum, int stat) {

		boolean response=false;
		FileInfo fi;
		if(stat==FileInfo.CREATE || stat==FileInfo.MODIFY){
			if(files.containsKey(checksum)){  //already Available
				fi=files.get(checksum);
				fi.addSeeder(p);
				fi.attachTag(fileName);
				response=true;
				System.out.println("SP.fileChanged(CREATE_OLD):" + fileName);
			}else{
				fi=new FileInfo(fileName,fileSize,checksum); //new Unique
				fi.addSeeder(p);
				files.put(checksum, fi);
				response=true;
				System.out.println("SP.fileChanged(CREATE_NEW):" + fileName);
			}
		}else if(stat==FileInfo.DELETE){ 
			fi=files.get(checksum);
			if(fi!=null){					//delete already avail
				fi.removeSeeder(p);
				if(fi.seederCount()<1){ //No seeder so remove file
					files.remove(checksum);
					System.out.println("SP.fileChanged(DELETE_Parmanent):" + fileName);
				}
				System.out.println("SP.fileChanged(DELETE):" + fileName);
				response=true;
			}else{							//delete already avail
				System.out.println(fi+ " is NOT on server to DELETE");
			}
		}
		return response;
	}


	@Override
	public boolean downloadFile(InetSocketAddress dest,String checksum,int sessionID) {
		System.out.println("SP.downloadFile()" + sessionID);
		if(!files.containsKey(checksum)){
			return false;
		}
		boolean success=true;
		FileInfo fi=files.get(checksum);
		HashSet<String> seeders=fi.getSeeders();
		
		int scnt=seeders.size();	//Source count
		int blkcnt=fi.getBlocksCount();//Block count
		
		if(scnt>blkcnt){	//Source are more than blocks, hence ignore some sources
			scnt=blkcnt;
		}
		
		int eqJ=blkcnt/scnt;	//equal Job
		System.out.println("BLOCK COUNT: " + blkcnt + " EqJ:" + eqJ + "  scnt:" + scnt );
		int exJ=blkcnt % eqJ;	//extra Job
		
		
		int blkfrm=0;	//from this block, included=block[blkfrm]
		int blkto;		//to this block, not included index=block[blkto]
		for(String s:seeders){
			blkto=blkfrm + eqJ;
			blkcnt-=eqJ;	//blocks reduced
			if(exJ>0){
				exJ--;
				blkto++;
				blkcnt--;
			}
			
			//ask s to send Blocks from blkfrm to blkto
			Peer p=peers.get(s);
			if(p==null){ //worst case, if peer unregistered
				
			}else{
				//TODO: Fix this
				p.uploadBlock(fi.getChecksum(),blkfrm,blkto,dest,sessionID); //This method should not block current control, it blocks only if requestor already have file.

				System.out.println(p.toString() + "--> FROM:" + blkfrm + " TO:" + blkto);
			}
			
			if(blkcnt<=0){
				//Ignore other sources
				break;
			}
			blkfrm=blkto;
		}
		
		return success;
	}

	@Override
	public boolean register(Peer p,boolean status) {
		System.out.println("register()  " + p.toString() + " : " + status);
		// add Peer into peerList
		boolean success=false;
		if(status){
			success=( peers.put(p.toString(), p) == null); /*//Returns:the previous value of the specified key in this hashtable, or null if it did not have one
															*If adding peer succeed? two peers with same name cant be added*/
		}else{
			success=(peers.remove(p.toString()) != null);
			//Peer unregistred, delete its file from files
			for(FileInfo fi:files.values()){		//TODO: Fix concurrent modification exception, fixed NOT yested
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

	@Override
	public FileInfo getFileInfo(String checksum) {
		return files.get(checksum);
	}

}
