package speer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.SynchronousQueue;

import com.FileInfo;
import com.Peer;

public class PeerStub implements Peer,Runnable {
/* Server side representative of Peer, creates as many instance as the number of peers*/
	private SuperPeerImpl sp; //needed to give callback(local)
	private Socket s;
	private ObjectOutputStream obos;
	private ObjectInputStream obis;
	private boolean connected;
	private boolean registered;
	private SynchronousQueue<Object> respBuf;
	
	String nick;
	
	public PeerStub(SuperPeerImpl sp,Socket s2){
		nick="NEW_PEER";
		s=s2;
		this.sp=sp;
		registered=false;
		connected=false;
		respBuf=new SynchronousQueue<Object>();
		initConnection();
	}
	
	void initConnection(){
		try {
			obis=new ObjectInputStream(s.getInputStream());
			obos=new ObjectOutputStream(s.getOutputStream());
			connected=true;
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
		
		try{
			while(connected){
				response=false;
				try {
					
					String ch=(String)obis.readObject();
					
					System.out.println(nick + " : " + ch);
					
					
					switch(ch){ //check the type of Request
					
					case "REG":
						nick=(String)obis.readObject();
						response=sp.register(this, true);
						obos.writeObject(new Boolean(response));
						if(response==true){
							registered=true; //Now peer's requests will be accepted
						}
						break;
						
					case "SEARCH":
						String query=(String)obis.readObject();
						obos.writeObject(sp.searchFile(query));
						break;
						
					case "DOWNLOAD":
						InetSocketAddress dest=(InetSocketAddress)obis.readObject();
						checksum=(String)obis.readObject();
						Integer sessionID=(Integer)obis.readObject();
						response=sp.downloadFile(dest,checksum,sessionID);
						obos.writeObject(new Boolean(response));
						break;
					
					case "FCHANGE":
						String fileName = (String)obis.readObject();
						Long fileSize=(Long)obis.readObject();
						checksum=(String)obis.readObject();
						int stat=(Integer)obis.readObject();
						response=sp.fileChanged(nick, fileName, fileSize, checksum, stat);
						obos.writeObject(new Boolean(response));
						break;

					case "GETFINFO":
						checksum=(String)obis.readObject();
						FileInfo fi=sp.getFileInfo(checksum);
						obos.writeObject(fi);
						break;
						
					/*###### Peer's Response for Callback RPC#############*/
					case "UPLOADBLOCKREPLY":
						Boolean resp1=(Boolean)obis.readObject();
						try {
							respBuf.put(resp1);
						} catch (InterruptedException e) {}
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
			System.out.println("Connection lost with :" + nick);
			sp.register(this, false); //Remove this peer from list of peers....This thread dies here
			try {s.close();	} catch (IOException e1) {	}
			
		} catch(IOException e){ e.printStackTrace();}
	}//run()

	
	@Override
	public boolean uploadBlock(String strfi,int blkfrm,int blkto,InetSocketAddress dest,int sessionID){
		Boolean b=false;
		try {
			obos.writeObject(new String("UPLOADBLOCK"));
			obos.writeObject(strfi);
			obos.writeObject(new Integer(blkfrm));
			obos.writeObject(new Integer(blkto));
			obos.writeObject(dest);
			obos.writeObject(new Integer(sessionID));
			
			try{
				//System.out.println("S:Response consumer waiting");
				b=(Boolean)respBuf.take();
				//System.out.println("S:Response consumed");
			}catch(InterruptedException e){}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}
	
	@Override
	public String toString(){
		return nick;
	}
}
