package com;


public interface Peer {

	public boolean uploadBlock(String checksum,int blkfrm,int blkto,String dest, int sessionID);
	public String toString();
}
