package speer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import com.FileInfo;
import com.Peer;
import com.SuperPeer;

public class PeerStub implements Peer,Runnable {
/* Server side representative of Peer, creates as many instance as the number of peers*/
	SuperPeer sp; //needed to give callback(local)
	Socket s;
	ObjectOutputStream obos;
	ObjectInputStream obis;
	boolean connFlag;
	boolean registered;
	SynchronousQueue<Object> respBuf;
	
	String nick;
	
	public PeerStub(SuperPeer sp,Socket s2){
		nick="NEW";
		s=s2;
		this.sp=sp;
		registered=false;
		connFlag=false;
		respBuf=new SynchronousQueue<Object>();
	}
	
	void initConnection(){
		try {
			obis=new ObjectInputStream(s.getInputStream());
			obos=new ObjectOutputStream(s.getOutputStream());
			connFlag=true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		/* This thread continously listen to Peer
		 */
		boolean response;
		String checksum=null;
		Integer id=null;
		initConnection(); //open streams for communication;
		
		try{		
			while(connFlag){
				response=false;
				try {
					
					String ch=(String)obis.readObject();
					
					System.out.println(nick + " : " + ch);
					
					
					switch(ch){ //check the type of Request
					
					case "REG":
						id=(Integer)obis.readObject();
						nick=(String)obis.readObject();
						response=sp.register(this, true);
						obos.writeObject(id);
						obos.writeObject(new Boolean(response));
						if(response==true){
							registered=true; //Now peer's requests will be accepted
						}
						break;
						
					case "SEARCH":
						id=(Integer)obis.readObject();
						String query=(String)obis.readObject();
						System.out.println("Remote Search:" + query);
						obos.writeObject(id);
						obos.writeObject(sp.searchFile(query));
						break;
						
					case "DOWNLOAD":
						id=(Integer)obis.readObject();
						String dest=(String)obis.readObject();
						dest.concat(s.getInetAddress().getHostAddress()); //address now becomes= IP:port
						checksum=(String)obis.readObject();
						Integer sessionID=(Integer)obis.readObject();
						response=sp.downloadFile(dest,checksum,sessionID);
						obos.writeObject(id);
						obos.writeObject(new Boolean(response));
						break;
					
					case "FCHANGE":
						id=(Integer)obis.readObject();
						String fileName = (String)obis.readObject();
						Long fileSize=(Long)obis.readObject();
						checksum=(String)obis.readObject();
						int stat=(Integer)obis.readObject();
						response=sp.fileChanged(nick, fileName, fileSize, checksum, stat);
						obos.writeObject(id);
						obos.writeObject(new Boolean(response));
						break;
				
					case "UPLOADBLOCKREPLY":
						Boolean resp1=(Boolean)obis.readObject();
						try {
							respBuf.offer(resp1, 5, TimeUnit.SECONDS);
						} catch (InterruptedException e) {}
						break;
					case "GETFINFO":
						id=(Integer)obis.readObject();
						checksum=(String)obis.readObject();
						FileInfo fi=sp.getFileInfo(checksum);
						obos.writeObject(id);
						obos.writeObject(fi);
						break;
					default:
						System.out.println("No method matching:" + ch);
						break;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			}//while
			
		} catch (SocketException e) {
			// Connection lost
			System.out.println("Connection lost with :" + nick);
			//e.printStackTrace();
			sp.register(this, false); //Remove this peer from list of peers....This thread dies here
			try {s.close();
			} catch (IOException e1) {	}
			
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	
	@Override
	public boolean uploadBlock(String strfi,int blkfrm,int blkto,String dest){
		Boolean b;
		try {
			obos.writeObject(new String("UPLOADBLOCK"));
			obos.writeObject(strfi);
			obos.writeObject(new Integer(blkfrm));
			obos.writeObject(new Integer(blkto));
			obos.writeObject(dest);
			
			b=(Boolean)respBuf.take();
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			b=false;
		}
		return b;
	}
	
	@Override
	public String toString(){
		return nick;
	}
}
