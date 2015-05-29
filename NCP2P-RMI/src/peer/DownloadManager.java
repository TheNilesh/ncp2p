package peer;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Hashtable;

import com.Constants;
import com.FileInfo;

public class DownloadManager {
	Hashtable<Integer,Download> downloads;
	Hashtable<Integer,Upload> uploads;
	PeerImpl proc;
	DatagramSocket ds;
	boolean connFlag;
	
	DownloadManager(PeerImpl proc,int port) throws SocketException{
		this.proc=proc;
		downloads=new Hashtable<Integer,Download>();
		connFlag=true;
		ds=new DatagramSocket(port);
	}
	
	Download addDownload(FileInfo fi,File localfile, int sessionID){
		//try{
				//Download d=new Download(this,localfile,sessionID);
				System.out.println("Download added "+ fi.toString() + " : " + localfile + " SessionID:" + sessionID);
				//downloads.put(new Integer(sessionID), d);
				//return d;
		//	}catch(SocketException e){
		//		e.printStackTrace();
		//}
		return null;
	}

	public void addUpload(FileInfo fi, File f, int sessionID){
			Upload u=new Upload(this,fi,f,sessionID);
			uploads.put(sessionID, u);
			
	}
	
	public void setComplete(Download d, boolean b, FileInfo fi) {
		System.out.println("Download Complete :" + d.fi.name + " Success?:" + b);
		if(b==true){
	//		proc.downloadComplete(d.f);
			//remove from downloads
		}
	}
	
	
	
	/*Downloader listens on UDP ######################################### */
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
			while(!connFlag)
			{
				System.out.println("Listening");
				ds.receive(p);
				byte[] temp=p.getData();	//array length is = buf.length, so we need another smaller array exactly = size of data
				byte[] packet= new byte[p.getLength()];
				System.arraycopy(temp,0,packet,0,p.getLength());
				if(processData(packet)){ //send ACK back
					dpACK.setSocketAddress(p.getSocketAddress());
					ds.send(dpACK);
				}
			 }//while
	}//listen
	
	boolean processData(byte[] packet){
		ByteArrayInputStream bis=new ByteArrayInputStream(packet);
		int sessionID=bis.read();
		Download d=downloads.get(new Integer(sessionID)); //Map with actual download
		if(d==null){
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
}
