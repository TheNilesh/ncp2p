package com;

import java.net.SocketAddress;

public interface Peer {

	public boolean uploadBlock(String checksum,int blkfrm,int blkto,String dest);
	public String toString();
}
