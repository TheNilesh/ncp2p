package com;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestCalc{
	
	public String calculateMD5(File f) {
		//System.out.println("Started digest");
		String strMD5="";

		try {
			MessageDigest complete;
			complete = MessageDigest.getInstance("MD5");
			//FileInputStream fis=new FileInputStream(f);
			RandomAccessFile fis=new RandomAccessFile(f,"r");
			byte data[]=new byte[1024];
				//org.apache.commons.codec.digest.DigestUtils.md5(fis);
			int numRead;
			do{
				numRead=fis.read(data);
				if(numRead>0)
					complete.update(data,0,numRead);
			}while(numRead!=-1);
			fis.close();	
			byte[] md5=complete.digest();
			
			
			for(int i=0;i<md5.length;i++){
				String str=Integer.toString((md5[i] & 0xff) + 0x100,16).substring(1);
				strMD5=strMD5.concat(str);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}catch(FileNotFoundException e){
			//May be The process cannot access the file because it is being used by another process..
			try {Thread.sleep(200);
			} catch (InterruptedException e1) {e1.printStackTrace();			}
			//recheck
			return calculateMD5(f);
			
		}catch(IOException e){
			System.out.println("error calculating md5");
		}
		return strMD5;
	}
}
