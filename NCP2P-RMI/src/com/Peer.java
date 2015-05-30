package com;

import java.net.InetSocketAddress;


public interface Peer {

	public boolean uploadBlock(String checksum,int blkfrm,int blkto,InetSocketAddress dest, int sessionID);
	public String toString();
}
