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
import java.util.Hashtable;

import com.Constants;
import com.FileInfo;

public class DownloadManager implements Runnable {
	Hashtable<Integer,Download> downloads;
	Hashtable<Integer,Upload> uploads;
	PeerImpl p;
	DatagramSocket ds;
	InetSocketAddress myaddress;
	boolean connFlag;
	
	DownloadManager(PeerImpl p,int port) throws SocketException{
		this.p=p;
		downloads=new Hashtable<Integer,Download>();
		uploads=new Hashtable<Integer,Upload>();
		connFlag=false;
		ds=new DatagramSocket(port);
		myaddress=null;
	}
	
	Download addDownload(FileInfo fi,File localfile, int sessionID){
		try{
				Download d=new Download(this,fi,localfile,sessionID);
				downloads.put(sessionID, d);
				System.out.println("Download added "+ fi.toString() + " : " + localfile + " SessionID:" + sessionID);
				p.view.addTask("DOWNLOAD",localfile.getName(),0,sessionID);
				
				//Get Address
				if(myaddress==null){
					loadMyAddress("49.248.108.146",4690); //contact STUN server
				}
				
				//start listening
				if(connFlag==false){ //id dm is idle, because no active download
					connFlag=true;
					new Thread(this).start();	//start listening
				}
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
	
	void loadMyAddress(String stunip,int stunport){
		byte[] tmp=new byte[1024];
		DatagramPacket dp;
		try {
			InetAddress ia=InetAddress.getByName(stunip);
			dp = new DatagramPacket(tmp,0,tmp.length,ia,stunport);
			ds.send(dp);
			ds.setSoTimeout(5000);
			ds.receive(dp);
			ds.setSoTimeout(0); //after this wait indefinitely
			tmp=dp.getData();
			String t1=(new String(tmp)).trim();
			String t2[]=t1.split(":");
			
			if(t2.length<2){
				myaddress=new InetSocketAddress(InetAddress.getLocalHost(),ds.getLocalPort());
				System.out.println("Error getting IP:PORT Assumed:" + myaddress);
				return;
			}
			
			String ip=t2[0];
			int port=Integer.parseInt(t2[1]);
			myaddress=new InetSocketAddress(ip,port);
			System.out.println("Got external address:" + ip + ":" + port);
		} catch (IOException e) {
			System.out.println("Error getting IP:PORT");
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
		// TODO Auto-generated method stub
		return ds.getLocalPort();
	}

	public void startUpload(int sessionID) {
		Upload u=uploads.get(new Integer(sessionID));
		u.startUpload();
	}
}
