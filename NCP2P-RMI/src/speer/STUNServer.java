package speer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class STUNServer implements Runnable{

	DatagramSocket ds;
	boolean connFlag;
	STUNServer(int port) throws SocketException{
		ds=new DatagramSocket(port);
		connFlag=true;
		new Thread(this).start();
	}
	
	@Override
	public void run(){
		byte[] buf=new byte[1024];
		DatagramPacket dp=new DatagramPacket(buf,buf.length);
		while(connFlag){
			try {
				ds.receive(dp);
				
				InetAddress ia=dp.getAddress();
				int port=dp.getPort();
				String site=ia.getHostAddress() + ":"+ port ;
				
				dp=new DatagramPacket(site.getBytes(),site.length(),ia,port);

				ds.send(dp);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
}
