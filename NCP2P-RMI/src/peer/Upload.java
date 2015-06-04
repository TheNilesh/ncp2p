package peer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.Constants;

public class Upload implements Runnable{
	public static final int TIMEOUT=5000;
	private DownloadManager dm;
	private File file;
	private int sessionID;
	private int blkfrm;
	private int blkto;
	private DatagramSocket ds;
	private InetSocketAddress ia;
	
	boolean on;

	public Upload(DownloadManager dm, File f, int blkfrm,	int blkto, int sessionID,InetSocketAddress ia) {
		this.dm=dm;
		this.file=f;
		this.sessionID=sessionID;
		this.blkfrm=blkfrm;
		this.blkto=blkto;
		this.ia=ia;
		on=false;
	}
	
	boolean startUpload(){
		try{
			if(!on){
				ds=new DatagramSocket();
				String ip=ia.getHostString();
				int port=ia.getPort();
				System.out.println("xxxxx"  + ip + ":" + port);
				//ds.connect(ia);
				ds.setSoTimeout(TIMEOUT);
				new Thread(this).start();
			}
		}catch(SocketException se){
			return false;
		}
		return on;
	}

	void readAndSend(){
		byte []tmp=null;
		
		try {
		  FileInputStream fis=new FileInputStream(file); //good for sequential access
		  tmp=new byte[Constants.BLOCK_SIZE];
		   try {
			   fis.skip(blkfrm*Constants.BLOCK_SIZE);
			   for(int i=blkfrm;i<blkto;i++){
			       int arrSize=fis.read(tmp);
			       if(arrSize<Constants.BLOCK_SIZE){
			    	   if(arrSize==-1){ //read failed
			    		   fis.close();
			    		   return;
			    	   }
			    	   
			    	   byte[] tmp2=new byte[arrSize];
			    	   System.arraycopy(tmp, 0, tmp2, 0, tmp2.length);
			    	   tmp=tmp2; //shrink array
			       }
			       
			       //tmp is block to send
			       if(send(i,tmp)==false){
		    		   //sending failed, maybe receiver absent
			    	   dm.p.view.updateProgress(sessionID,-1);
		    		   return;
		    	   }
			       dm.p.view.updateProgress(sessionID,i-blkfrm);
			   }//for loop
		       
		   }catch(IOException e){
			   System.out.println("File reading error");
			   e.printStackTrace();
		   }finally {
		      fis.close();
		   }
		}catch (IOException ex) {
			//File Not found
		   	ex.printStackTrace();
		}
		
	}
	
	boolean send(int blknum, byte[]payload){
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try{
			baos.write(sessionID); //session identifying 
			baos.write(blknum);		//index of block
			baos.write(payload);	//actual data
			
			byte[] packet=baos.toByteArray();
			DatagramPacket dp=new DatagramPacket(packet,packet.length);
				
				dp.setSocketAddress(ia);
				String ip=ia.getAddress().getHostAddress();
				int port=ia.getPort();
				dp.setAddress(InetAddress.getByName(ip));
				dp.setPort(port);
				
				System.out.println("Sending to :" + ip + ":"+ port);
				
				ds.send(dp);
				ds.receive(dp);		//wait for max 3 seconds
				//ACK recvd
				
				return true;
			}catch(SocketTimeoutException se){
			System.out.println("ACK not received. Cancelling transfer.");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;}
	}
	
	@Override
	public void run() {
		readAndSend();
		
	}
}
