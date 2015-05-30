package peer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
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
	private boolean downloadComplete;
	private byte[] blocks;
	RandomAccessFile out;
	private static final int REC_DATA=11;
	private static final int REC_CODE=12;
	
		Download(DownloadManager dm,FileInfo fi,File f,int sessionID) throws SocketException{
			this.dm=dm;
			downloadComplete=false;
			this.fi=fi;
			this.localfile=f;
			this.sessionID=sessionID;
			this.blocks=fi.getBlocks();
			try {
				out=new RandomAccessFile(localfile,"rw");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		void unmarshal(byte[] packet){
			//split header and call storeToFile
			byte[]payload = null;
			String checksum;
			int blockNumber;
			byte[] tmp=new byte[1];
			byte[] chk=new byte[16];
			ByteArrayInputStream bis=new ByteArrayInputStream(packet);
			
			try {
				bis.read(); //sessionID
				bis.read(tmp); //what to do with block? is it data or code
				bis.read(chk); //checksum
				checksum=Constants.bytesToHex(chk);//convert checksum back into String
				blockNumber=bis.read();	//Which location
				payload=new byte[bis.available()];
				bis.read(payload); //read data
				//System.out.println("Received block [" + blockNumber + "] of " + fi.getFile().getName());
				
				if(!checksum.equals(fi.getChecksum())){
					System.out.println("Error :checksum does not match.");
				}else{
					if(tmp[0]==1){ 							//type of packet is data_block
						dataReceived(blockNumber,payload);
						checkIfComplete();
					}else if(tmp[0]==2){						//packet contains code
						codeReceived(blockNumber,payload);
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private void codeReceived(int blockNumber, byte[] payload) {
			// Consider this as contigeous blocks [##], [#_], [_#],[X#],[#X]
			
			byte[] tmp;
			//pair with blockNumber+1, blockNumber is always even if its payload is CODE
			if(blocks[blockNumber]==REC_DATA){ //[#_]
				tmp=readBlock(blockNumber);
				tmp=exorBlocks(payload,tmp);
				storeToFile(blockNumber+1,tmp);
				blocks[blockNumber+1]=REC_DATA;	//mark block received
			}else if(blocks[blockNumber+1]==REC_DATA){ //[_#]
				tmp=readBlock(blockNumber+1);
				tmp=exorBlocks(payload,tmp);
				storeToFile(blockNumber,tmp);
				blocks[blockNumber+1]=REC_DATA;	//mark block received
			}else{								// [_ _],
				storeToFile(blockNumber,payload);
				storeToFile(blockNumber+1,payload);
				blocks[blockNumber]=REC_CODE;
				blocks[blockNumber+1]=REC_CODE; //[XX]
			}

		}
		
		private void dataReceived(int blockNumber,byte[] payload){
			byte[] tmp;
	
			if(blockNumber%2==0){ //[#_] this is lower block
				if(blocks[blockNumber]==REC_CODE){ //[XX]
					tmp=readBlock(blockNumber); //this is code
					tmp=exorBlocks(tmp,payload);
					storeToFile(blockNumber,payload);
					blocks[blockNumber]=REC_DATA;
					storeToFile(blockNumber+1,tmp);
					blocks[blockNumber+1]=REC_DATA;
				}else if(blocks[blockNumber]==REC_DATA){
					//we already have that block
				}else{
					storeToFile(blockNumber,payload);
					blocks[blockNumber]=REC_DATA;
				}
				
				
			}else{ //[_#], upper block
				if(blocks[blockNumber]==REC_CODE){ //[XX]
					tmp=readBlock(blockNumber); //this is code
					tmp=exorBlocks(tmp,payload);
					storeToFile(blockNumber,payload);
					blocks[blockNumber]=REC_DATA;
					storeToFile(blockNumber-1,tmp);
					blocks[blockNumber-1]=REC_DATA; //[##]
				}else if(blocks[blockNumber]==REC_DATA){
					//we already have that block
				}else{
					storeToFile(blockNumber,payload);
					blocks[blockNumber]=REC_DATA;
				}
				
			}
		}
		
		private byte[] exorBlocks(byte[] b1, byte[] b2) {
			byte[] exored=new byte[b1.length];
			
			for(int i=0;i<b1.length;i++){ //exoring
				exored[i]=(byte) ((byte) b1[i]^b2[i]);
			}
			
			return exored;
		}
		
		private byte[] readBlock(int blockNumber) {
			System.out.println("Saved:" + localfile.getPath());
			byte []tmp=null;
			
			try {
			  FileInputStream fis=new FileInputStream(localfile);
			  tmp=new byte[Constants.BLOCK_SIZE];
			   try {
				   fis.skip(blockNumber*Constants.BLOCK_SIZE);
			       int arrSize=fis.read(tmp);
			       
			       if(arrSize<Constants.BLOCK_SIZE){
			    	   if(arrSize==-1){ //read failed
			    		   fis.close();
			    		   return null;
			    	   }
			    	   byte[] tmp2=new byte[arrSize];
			    	   System.arraycopy(tmp, 0, tmp2, 0, tmp2.length);
			    	   tmp=tmp2; //shrink array
			       }
			       
			   } finally {
			      fis.close();
			   }
			} catch (IOException ex) {
			   	ex.printStackTrace();
			}
			return tmp;
		}
		
		private void storeToFile(int blockNumber, byte[] payload){
			try {
			   try {
				   System.out.println("Putting data at loc" + blockNumber*Constants.BLOCK_SIZE + " File:" + localfile.getPath());
			       out.seek(blockNumber*Constants.BLOCK_SIZE);
			       out.write(payload);
			      
			   } finally {
			       out.close();
			   } 
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
		
		public boolean checkIfComplete(){
			for(int i=0;i<blocks.length;i++){
				if(blocks[i]!=REC_DATA){
					System.out.println("Block [" + i + "] not received yet");
					return false;
				}
			}
			
			FileInfo downfi=new FileInfo(localfile); //checksum autoCalc
			String expChecksum=fi.getChecksum();
			String actChecksum=downfi.getChecksum();
			if(expChecksum.equals(actChecksum)){
				downloadComplete=true;
				System.out.println("Download completed verified");
				dm.setComplete(this,true,downfi);
			}else{
				System.out.println("Download checksum Failed");
				dm.setComplete(this,false,downfi);
			}
			
			return true;
		}
}