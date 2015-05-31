package com;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

public class FileInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public static final int CREATE=1;
	public static final int DELETE=2;
	public static final int MODIFY=3;	
	
	private String checksum;
	public String name;
	private HashSet<String> tags;
	private HashSet<String> hasFile;
	private transient File file;
	private long len;
	
	public FileInfo(File f){
		this.file=f;
		tags=new HashSet<String>();
		hasFile=new HashSet<String>();
		this.len=f.length();
		this.name=f.getName();
		attachTag(name);
		try{
			DigestCalc dc=new DigestCalc();
			checksum=dc.calculateMD5(f);
		}catch(Exception e){
			checksum="ERROR";
			System.out.println("Error calculating checksum");
		}
		
	}
	
	public FileInfo(String fileName, long fileSize, String checksum2) {
		this.name=fileName;
		this.len=fileSize;
		this.checksum=checksum2;
		tags=new HashSet<String>();
		hasFile=new HashSet<String>();
		attachTag(fileName);
	}
	public String toString(){
		return name;
	}
	
	public File getFile(){
			return file;
	}
	
	public void attachTag(String tag){ //called by server
			tags.add(tag.toLowerCase());
	}
	
	public boolean addSeeder(String p){
		return hasFile.add(p);
	}
	
	public boolean tagMatches(String toSearch){
		toSearch.toLowerCase();
		Iterator<String> iter = tags.iterator();
		 while(iter.hasNext()){
		        String s = iter.next();
		        if (s.contains(toSearch)){
		            return true;
		        }
		    }
		 return false;
	}

	public HashSet<String> getTags(){
		return tags;
	}
	public String getChecksum(){
		return checksum;
	}
	
	public void setFile(File lf) {
		this.file=lf;
	}
	
	public void removeSeeder(String p) {
		hasFile.remove(p);
	}
	
	public HashSet<String> getSeeders(){
		HashSet<String> copied=new HashSet<String>(hasFile);
		return copied;
	}
	
	public long getLen(){
		return len;
	}
	
	public int getBlocksCount(){
		int fileSize=(int)len;
		int blkCnt=fileSize/Constants.BLOCK_SIZE;
		blkCnt=blkCnt+ (fileSize%Constants.BLOCK_SIZE == 0?0:1);
		return blkCnt;
	}
	
	public int seederCount() {
		return hasFile.size();
	}
}