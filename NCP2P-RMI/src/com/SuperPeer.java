package com;

import java.net.InetSocketAddress;
import java.util.HashSet;

import peer.NotConnectedException;

public interface SuperPeer {

	public HashSet<FileInfo> searchFile(String query)  throws NotConnectedException;
	public boolean downloadFile(InetSocketAddress dest,String checksum,int sessionID) throws NotConnectedException;
	boolean register(Peer p,boolean status);
	public boolean fileChanged(String nick, String fileName, long fileSize, String strfi, int stat)  throws NotConnectedException;
	public FileInfo getFileInfo(String checksum)  throws NotConnectedException;
}
