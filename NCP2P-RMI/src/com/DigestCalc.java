package com;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestCalc{
	
	public String calculateMD5(File f) throws IOException{
		//System.out.println("Started digest");
		String strMD5="";
		FileInputStream fis=new FileInputStream(f);
		byte data[]=new byte[1024];
			//org.apache.commons.codec.digest.DigestUtils.md5(fis);
		MessageDigest complete;
		try {
			complete = MessageDigest.getInstance("MD5");
		
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
				//System.out.print(str);
				strMD5=strMD5.concat(str);
			}
			//System.out.println("Digest completed.");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return strMD5;
	}
}
