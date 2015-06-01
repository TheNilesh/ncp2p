package peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

import com.FileInfo;
import com.Host;
import com.Peer;
import com.SuperPeer;

public class SuperPeerStub implements SuperPeer,Runnable{
/* Peer Side Representative of SuperPeer
 * Job of this class is to Pack Parameters and send to server, unpack returned result and give back*/
	
	/*TODO: respBuf() waits indefinitely.
	 * 
	 */
	public static final long TIMEOUT=4500;
	Random r;	
	Socket s;
	LinkedList<Host> superPeers;
	PeerImpl p;
	
	ObjectOutputStream obos;
	ObjectInputStream obis;
	boolean connFlag;
	
	SynchronousQueue<Object> respBuf;

	public SuperPeerStub(PeerImpl p, LinkedList<Host> superpeers) {
		this.p=p;
		respBuf=new SynchronousQueue<Object>();
		r=new Random();
		this.superPeers=superpeers;
		connFlag=false;
	}

	private void initConnection(){
		connFlag=false;
		int i=0;
		int maxAttempt=superPeers.size();
		Host a;
		while(i<maxAttempt){
			a=superPeers.getFirst();
			System.out.println("Contacting SuperPeer :"  + a);
			try {
				s=new Socket(a.getIp(),a.getPort());
				obos=new ObjectOutputStream(s.getOutputStream());
				obis=new ObjectInputStream(s.getInputStream());
				connFlag=true;
				p.view.setInfo("STAT", "Online");
				p.view.setInfo("SP", a.toString());
				return;
			}catch(ConnectException e){
				System.out.println("Failed to connect " + a );
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to due to Exception " + a );
			}
			
			a=superPeers.removeFirst();
			superPeers.addLast(a);
			i++;
		}
		
		p.view.setInfo("STAT", "Failed to Connect");
	}
	
	@Override
	public synchronized boolean fileChanged(String nick, String fileName, long fileSize, String strfi, int stat) {
		boolean response=false;
		while(!connFlag); //wait for connect
		try {
			obos.writeObject(new String("FCHANGE"));
			obos.writeObject(fileName);
			obos.writeObject(new Long(fileSize));
			obos.writeObject(strfi);
			obos.writeObject(new Integer(stat));
			
			try {
				response=(Boolean)respBuf.take();
			} catch (InterruptedException e) {e.printStackTrace();	}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public HashSet<FileInfo> searchFile(String query) {
		while(!connFlag); //wait for connect
		HashSet<FileInfo> hs=new HashSet<FileInfo>();
		try {
			obos.writeObject(new String("SEARCH"));
			obos.writeObject(query);
			try{
				hs=(HashSet<FileInfo>)respBuf.take();
			} catch (InterruptedException e) {e.printStackTrace();	}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return hs;
	}

	@Override
	public synchronized boolean downloadFile(InetSocketAddress sa, String checksum,int sessionID) {
		boolean response=false;
		while(!connFlag); //wait for connect
		try {
			obos.writeObject(new String("DOWNLOAD"));
			obos.writeObject(sa);
			obos.writeObject(checksum);
			obos.writeObject(new Integer(sessionID));

			try{
				response=(Boolean)respBuf.take();
			} catch (InterruptedException e) {e.printStackTrace();	}
			
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
			obos.writeObject(p.toString());

			try{
			response=(Boolean)respBuf.take(); /* This will wait till run() enters some value in this Syncqueue*/
			} catch (InterruptedException e) {e.printStackTrace();	}

		} catch (IOException e) {
			//connection error
			e.printStackTrace();
		}
		return response;
	}
	
	@Override
	public FileInfo getFileInfo(String checksum) {
		try {
			obos.writeObject(new String("GETFINFO"));
			obos.writeObject(checksum);
			try{
				FileInfo fi= (FileInfo)respBuf.take();
				return fi;
			} catch (InterruptedException e) {e.printStackTrace();	}

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	/*Receiving Server's RPC call and collecting responses of our RPC to server*/
	@Override
	public void run() {
		/* This thread continously listen to Server through PeerStub*/
		System.out.println("Connecting to SuperPeer");
		initConnection();
		//System.out.println("Connected to SuperPeer");
		
		try{	
			while(connFlag){
				try {
					String ch="RESPONSE";
					
					Object obj=obis.readObject();
					System.out.println("Recvd:" + obj);
					if(obj instanceof String){
						ch=(String)obj;
				//		System.out.println("Server : " + ch);
					}
					
					switch(ch){ //check the type of Response
					
					case "UPLOADBLOCK":
						String strfi=(String)obis.readObject();
						Integer blkfrm=(Integer)obis.readObject();
						Integer blkto=(Integer)obis.readObject();
						InetSocketAddress dest=(InetSocketAddress)obis.readObject();
						Integer sessionID=(Integer)obis.readObject();
						Boolean b1=(Boolean)p.uploadBlock(strfi, blkfrm, blkto, dest,sessionID);
						//System.out.println("Writing back to server");
						obos.writeObject(new String("UPLOADBLOCKREPLY"));
						obos.writeObject(b1);
						//System.out.println("Written back to server");
						break;
					case "RESPONSE":
						try {
							//System.out.println("Response produced");
							respBuf.put(obj);
							//System.out.println("Response consumed");
						} catch (InterruptedException e) {e.printStackTrace();}
						break;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();}
			}//while loop ends
		}catch (SocketException e) {
			// Connection lost
			System.out.println("Disconnected from superpeer");
			connFlag=false;
		}catch(IOException e){
			System.out.println("Listening stopped due to IOException");
			connFlag=false;
		}
		p.view.setInfo("STAT", "Offline");
		System.out.println("Reconnecting...");
		initConnection(); //Reconnect
	}//run()

}
