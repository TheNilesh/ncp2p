package speer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SPServer implements Runnable{
	/* Runs ServerSocket and handles new Connection Requests 
	 */
	SuperPeerImpl sp;
	ServerSocket srv;
	boolean connFlag;
	ThreadGroup connections;
	int cnt;
	
	SPServer(SuperPeerImpl sp,int port) throws IOException{
		this.sp=sp;
		srv=new ServerSocket(port);
		connFlag=true;
		connections=new ThreadGroup("Connections");
		cnt=0;
		new Thread(this).start();
	}

	@Override
	public void run() {
		while(connFlag){
			try {
				Socket s=srv.accept();
				Thread t=new Thread(connections,new PeerStub(sp,s),"CON" + cnt++);
				t.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
