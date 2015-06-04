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
	private Socket s;
	public Thread recvThrd;
	private LinkedList<Host> superPeers;
	private PeerImpl p;
	
	private ObjectOutputStream obos;
	private ObjectInputStream obis;
	boolean connected;
	
	private SynchronousQueue<Object> respBuf;

	public SuperPeerStub(PeerImpl p, LinkedList<Host> superpeers) {
		this.p=p;
		respBuf=new SynchronousQueue<Object>();
		//r=new Random();
		this.superPeers=superpeers;
		connected=false;
		initConnection();
	}

	private void initConnection(){
		connected=false;
		int i=0;
		int maxAttempt=superPeers.size();
		Host a;
		while(i<maxAttempt){
			p.view.setInfo("STAT", "Offline");
			a=superPeers.getFirst();
			System.out.println("Contacting SuperPeer :"  + a);
			try {
				s=new Socket(a.getIp(),a.getPort());
				obos=new ObjectOutputStream(s.getOutputStream());
				obis=new ObjectInputStream(s.getInputStream());
				connected=true;
				recvThrd=new Thread(this); //Start receiving msgs
				recvThrd.start();
				p.view.setInfo("STAT", "Connected");
				if(!register(p,true)){//if nickName already taken
					p.nick = p.view.getInputString("Nickname not available. Choose another.");
					if(p.nick==null){
							p.nick="Peer" + new Random().nextInt(10);
					}
				}
				
				p.view.setInfo("SP", a.toString());
				p.view.setInfo("PNAME", p.nick);
				p.view.setInfo("STAT", "Online");
				p.sendAllFilesInfo();	//ask p to update metadata to sp
				return;
			}catch(ConnectException e){
				System.out.println("Failed to connect " + a );
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to connect due to Exception " + a );
			}
			
			a=superPeers.removeFirst();
			superPeers.addLast(a);
			i++;
		}
		
		p.view.setInfo("STAT", "Failed to Connect");
		System.out.println("retrying after 10 seconds.");
		//create Timer ask call back
	}
	
	@Override
	public synchronized boolean fileChanged(String nick, String fileName, long fileSize, String strfi, int stat) throws NotConnectedException{
		boolean response=false;
		if(!connected){ throw new NotConnectedException(); }
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
	public HashSet<FileInfo> searchFile(String query) throws NotConnectedException{
		if(!connected){ throw new NotConnectedException(); }
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
	public synchronized boolean downloadFile(InetSocketAddress sa, String checksum,int sessionID) throws NotConnectedException{
		boolean response=false;
		if(!connected){ throw new NotConnectedException(); }
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
	public FileInfo getFileInfo(String checksum) throws NotConnectedException{
		if(!connected){ throw new NotConnectedException(); }
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
		
		try{	
			while(connected){
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
			connected=false;
		}catch(IOException e){
			System.out.println("Listening stopped due to IOException");
			connected=false;
		}
		System.out.println("Reconnecting...");
		initConnection(); //Reconnect
	}//run()

}
