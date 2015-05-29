package com;

import java.net.SocketAddress;

public interface Peer {

	public boolean uploadBlock(String checksum,int blkfrm,int blkto,SocketAddress dest);
	public String toString();
}
