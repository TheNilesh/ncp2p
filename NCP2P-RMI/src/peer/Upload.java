package peer;

import java.io.File;

import com.FileInfo;

public class Upload implements Runnable{
	private DownloadManager dm;
	private File file;
	private int sessionID;
	private int blkfrm;
	private int blkto;
	
	boolean on;

	public Upload(DownloadManager dm, File f, int blkfrm,	int blkto, int sessionID) {
		this.dm=dm;
		this.file=f;
		this.sessionID=sessionID;
		this.blkfrm=blkfrm;
		this.blkto=blkto;
		on=false;
	}
	
	boolean startUpload(){
		if(!on){
			new Thread(this).start();
		}
		return on;
	}

	void processStart(){
		//open file
		RandomAccessFile fis=new RandomAccessFile(file);
		
	}
	
	
	@Override
	public void run() {
		processStart();
		
	}

}
