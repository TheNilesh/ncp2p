package peer;

import java.io.File;

import com.FileInfo;

public class Upload {
	private DownloadManager dm;
	private File file;
	private FileInfo fi;
	private int sessionID;

	public Upload(DownloadManager dm, FileInfo fi, File f,	int sessionID) {
		this.dm=dm;
		this.fi=fi;
		this.file=f;
		this.sessionID=sessionID;
	}
	
	boolean startUpload(){
		
		return true;
	}
	
	boolean stopUpload(){
		return true;
	}

}
