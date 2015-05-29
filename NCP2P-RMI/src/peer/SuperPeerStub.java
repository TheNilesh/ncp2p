package peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

import com.FileInfo;
import com.Peer;
import com.SuperPeer;
import com.TransferMap;

public class SuperPeerStub implements SuperPeer,Runnable{
/* Peer Side Representative of SuperPeer
 * Job of this class is to Pack Parameters and send to server, unpack returned result and give back*/
	public static final long TIMEOUT=4500;
	Random r;
	
	Socket s;
	String site;
	int port;
	
	PeerImpl p;
	ObjectOutputStream obos;
	ObjectInputStream obis;
	boolean connFlag;
	
	SynchronousQueue<Object> respBuf;
	TransferMap<Integer,Object> buffer;
	
	public SuperPeerStub(PeerImpl p, String site,int port){
		this.site=site;
		this.port=port;
		this.p=p;
		respBuf=new SynchronousQueue<Object>();
		r=new Random();
		buffer=new TransferMap<Integer,Object>(); //buffer size=16
	}
	
	private void initConnection(){
		connFlag=false;
		try {
			s=new Socket(site,port);
			obos=new ObjectOutputStream(s.getOutputStream());
			obis=new ObjectInputStream(s.getInputStream());
			connFlag=true;
		} catch (IOException e) {
			e.printStackTrace();
			//execute alternate SuperPeer connection
		}
	}
	
	@Override
	public synchronized boolean fileChanged(String nick, String fileName, long fileSize, String strfi, int stat) {
		boolean response=false;
		while(!connFlag); //wait for connect
		try {
			obos.writeObject(new String("FCHANGE"));
			Integer id=writeId();
			obos.writeObject(fileName);
			obos.writeObject(new Long(fileSize));
			obos.writeObject(strfi);
			obos.writeObject(new Integer(stat));

			response=(Boolean)buffer.getAndWait(id,TIMEOUT); //mapping found to msg sent
			//response=(Boolean)respBuf.take();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	

	@Override
	public HashSet<FileInfo> searchFile(String query) {
		while(!connFlag); //wait for connect
		HashSet<FileInfo> hs=new HashSet<FileInfo>();
		try {
			obos.writeObject(new String("SEARCH"));
			Integer id=writeId();
			System.out.println(query);
			obos.writeObject(query);
			
			hs=(HashSet<FileInfo>)buffer.getAndWait(id,TIMEOUT);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return hs;
	}

	@Override
	public boolean downloadFile(String sa, String checksum,int sessionID) {
		boolean response=false;
		while(!connFlag); //wait for connect
		try {
			obos.writeObject(new String("DOWNLOAD"));
			Integer id=writeId();
			obos.writeObject(sa);
			obos.writeObject(checksum);
			obos.writeObject(new Integer(sessionID));

			response=(Boolean)buffer.getAndWait(id,TIMEOUT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return response;
	}

	@Override
	public synchronized boolean register(Peer p, boolean status) {
		boolean response=false;
		while(!connFlag); //wait for connect
		try {
			obos.writeObject(new String("REG"));
			Integer id=writeId();
			obos.writeObject(p.toString());

			response=(Boolean)buffer.getAndWait(id,TIMEOUT); /* This will wait till run() enters some value in this Syncqueue*/

		} catch (IOException e) {
			//connection error
			e.printStackTrace();
		}
		return response;
	}

	@Override
	public void run() {
		/* This thread continously listen to Server through PeerStub*/
		System.out.println("Connecting SP:" + site + ":" + port);
		initConnection();
		System.out.println("Connected to SP");
		
		try{	
			while(connFlag){
				try {
					Object obj=obis.readObject();
					System.out.println("Recvd:" + obj);
					
					//Server wants to execute Client(Peer) method
					if(obj instanceof String){	//
						String callback=(String)obj;
						switch(callback){
						case "UPLOADBLOCK":
							String strfi=(String)obis.readObject();
							Integer blkfrm=(Integer)obis.readObject();
							Integer blkto=(Integer)obis.readObject();
							String dest=(String)obis.readObject();
							Boolean b1=(Boolean)p.uploadBlock(strfi, blkfrm, blkto, dest);
							obos.writeObject(new String("UPLOADBLOCKREPLY"));
							obos.writeObject(b1);
							continue; //loop continue
						}	
					}
					
					//Server sent response after method execution
					Integer id=(Integer)obis.readObject();
					obj=obis.readObject();
					buffer.putIfAbsent(id,obj);
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();}
			}
		}catch (SocketException e) {
			// Connection lost
			System.out.println("Disconnected:" + p.nick);
			//execute connection to next Super peer
			//e.printStackTrace();
			connFlag=false;
		}catch(IOException ie){
			ie.printStackTrace();
			connFlag=false;
		}
	}

	@Override
	public FileInfo getFileInfo(String checksum) {
		try {
			obos.writeObject(new String("GETFINFO"));
			Integer id=writeId();
			obos.writeObject(checksum);

			return (FileInfo)buffer.getAndWait(id,TIMEOUT);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	Integer writeId() throws IOException{
		Integer id=new Integer(r.nextInt());
		buffer.put(id,null);
		obos.writeObject(id);
		return id;
	}

}
