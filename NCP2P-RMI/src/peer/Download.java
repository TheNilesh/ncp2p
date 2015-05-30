package peer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.Constants;
import com.FileInfo;
/*fi -- FileInfo remote info
 * f <-- local file to be downloaded
 * 
 */

public class Download{
	DatagramSocket ds;
	int sessionID;
	int progress;
	Thread thread;
	private DownloadManager dm;
	FileInfo fi;
	File localfile;
	byte[] flags;
	int blkcnt;
	int blkgot;
	RandomAccessFile out;
	
		Download(DownloadManager dm,FileInfo fi,File f,int sessionID) throws SocketException,FileNotFoundException{
			this.dm=dm;
			this.fi=fi;
			this.localfile=f;
			this.sessionID=sessionID;
			flags=new byte[fi.getBlocksCount()];
			blkcnt=fi.getBlocksCount();
			blkgot=0;
			out=new RandomAccessFile(localfile,"rw");
	
		}

		void unmarshal(byte[] packet){
			//split header and call storeToFile
			byte[]payload = null;
			int blockNumber;
			ByteArrayInputStream bis=new ByteArrayInputStream(packet);
			bis.read(); //sessionID
			try {
				blockNumber=bis.read();	//Which location
				payload=new byte[bis.available()];
				bis.read(payload); //read data
				//type of packet is data_block
				if(flags[blockNumber]==0){
					storeToFile(blockNumber,payload);
					blkgot++;
				}
				if(blkgot==blkcnt){
					System.out.println("File received");
					dm.downloadOver(sessionID);
					out.close();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	
		private synchronized void storeToFile(int blockNumber, byte[] payload){
			try {
				   System.out.println("Putting data at loc" + blockNumber*Constants.BLOCK_SIZE + " File:" + localfile.getPath());
			       out.seek(blockNumber*Constants.BLOCK_SIZE);
			       out.write(payload);
			} catch (IOException ex) {
			   	ex.printStackTrace();
			}
		}
		
		public File createNullFile() throws IOException,FileNotFoundException{
			if(localfile.exists())
				return localfile;	//file already available no create
			byte b=0x00; //null
			FileOutputStream fos=new FileOutputStream(localfile);
			long fileSize=fi.getLen();
			for(int i=0;i<fileSize;i++)
				fos.write(b);//write null
			fos.close();
			return localfile;
		}
}