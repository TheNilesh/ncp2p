package peer;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.LinkedList;

import com.Constants;
import com.FileInfo;
import com.Host;

public class DownloadManager implements Runnable {
	
	final int TIMEOUT=5000;
	Hashtable<Integer,Download> downloads;
	Hashtable<Integer,Upload> uploads;
	PeerImpl p;
	DatagramSocket ds;
	
	LinkedList<Host> stunservers;
	
	InetSocketAddress myaddress;
	boolean connFlag;
	
	DownloadManager(PeerImpl p,LinkedList<Host> stuns){
		this.p=p;
		downloads=new Hashtable<Integer,Download>();
		uploads=new Hashtable<Integer,Upload>();
		connFlag=false;
		this.stunservers=stuns;
		myaddress=null;
	}
	
	Download addDownload(FileInfo fi,File localfile, int sessionID){
		try{
				//Contact STUN server and receive global IP:PORT
				if(myaddress==null){
					initSocket();
				}
				
				//start listening
				if(connFlag==false){ //id dm is idle, because no active download
					connFlag=true;
					new Thread(this).start();	//start listening
				}
				
				Download d=new Download(this,fi,localfile,sessionID);
				downloads.put(sessionID, d);
				System.out.println("Download added "+ fi.toString() + " : " + localfile + " SessionID:" + sessionID);
				p.view.addTask("DOWNLOAD",localfile.getName(),0,sessionID);
				
				return d;
		}catch (FileNotFoundException e2){
			e2.printStackTrace();
		}catch(SocketException e1){
				e1.printStackTrace();
		}
		return null;
	}
	
	Upload addUpload(File f, String strfi, int blkfrm, int blkto, int sessionID,InetSocketAddress dest) {

		Upload u=new Upload(this,f,blkfrm,blkto,sessionID,dest);
		uploads.put(sessionID,u);
		System.out.println("Upload added "+ f.getName() + " : " +strfi + " SessionID:" + sessionID  + " to " + dest);
		p.view.addTask("UPLOAD",f.getName(),blkto-blkfrm,sessionID);
		return u;
	}
	
	void taskComplete(int sessionID){
		
		Download d=downloads.get(sessionID);
		if(d!=null){
			p.downloadComplete(d.localfile);
		}
		
	}
	
	InetSocketAddress getExternalAddress(){
		return myaddress;
	}
	
	void initSocket(){

		byte[] tmp=new byte[1024];
		int i=0;
		int maxAttempt=5;
		DatagramPacket dp;
		
		try {
			ds=new DatagramSocket();
			ds.setSoTimeout(TIMEOUT);
			
			while(i<maxAttempt){
				Host h=stunservers.getFirst();
				try
				{
					InetAddress ia=InetAddress.getByName(h.getIp());
					dp = new DatagramPacket(tmp,0,tmp.length,ia,h.getPort());	//TODO: initialise packet outside of for-loop
	
					ds.send(dp);	//Request to STUN server
				
					ds.receive(dp);		//may raise SocketTimeoutExc
					
					tmp=dp.getData();
					String t1=(new String(tmp)).trim();
					
					String t2[]=t1.split(":");	//may raise ArrayOutOfBoundExc
					String ip=t2[0];
					int port=Integer.parseInt(t2[1]);
					
					myaddress=new InetSocketAddress(ip,port);
	
					System.out.println("Got external address: " + ip + ":" + port);
					ds.setSoTimeout(0); //after this no need to timeout
					return;
			//	}catch(ArrayOutOfBoundException e){
			//		System.out.println("Invalid STUN protocol : " + h);
				}catch(SocketTimeoutException e){
					System.out.println("STUN Server:" + h + " , did not replied within " + TIMEOUT + " milliseconds.");
				}catch(UnknownHostException e){
					System.out.println("STUN Server:" + h + " is invalid");
				}catch(IOException e){
					System.out.println("STUN Server:" + h + " I/O connection error.");
				}
				
				h=stunservers.removeFirst();
				stunservers.addLast(h);
				i++;
			}
		} catch (SocketException e) {
			System.out.println("Failed creating Socket.");
			e.printStackTrace();
		}
	}
	
	/*Downloader listens on UDP ######################################### */
	@Override
	public void run(){
		try{
				listen();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	void listen() throws IOException{
		byte buf[]=new byte[Constants.BLOCK_SIZE + 20];	//18 bytes header
		byte[] tmp1="ACK".getBytes();
		DatagramPacket dpACK = new DatagramPacket(tmp1,tmp1.length);
		DatagramPacket p=new DatagramPacket(buf,buf.length);
			while(connFlag)
			{
				System.out.println("Listening");
				ds.receive(p);
				byte[] temp=p.getData();	//array length is = buf.length, so we need another smaller array exactly = size of data
				
				byte[] packet= new byte[p.getLength()];		//sessionID removed
				System.arraycopy(temp,0,packet,0,p.getLength());
				//System.out.println("Recvied UDP:" + new String(packet));
				if(processPacket(packet)){	//if packet is valid
					System.out.println("Sending ACK back");
					dpACK.setSocketAddress(p.getSocketAddress());
					ds.send(dpACK);
				}else{
					System.out.println("Invalid packet.. rejected");
				}
			 }//while
	}//listen
	
	boolean processPacket(byte[] packet){
		
		ByteArrayInputStream bais=new ByteArrayInputStream(packet);
		int sessionID=bais.read();
		System.out.println("Packet SessionID:" + sessionID);
		Download d=downloads.get(new Integer(sessionID));
		if(d==null){ 			//No sessionID available
			return false;
		}
		
		(new Thread(){
				public void run(){
					d.unmarshal(packet);
				}
		}).start();
		
		return true;
	}

	public int getPort() {
		return ds.getLocalPort();
	}

	public void startUpload(int sessionID) {
		Upload u=uploads.get(new Integer(sessionID));
		u.startUpload();
	}
}
