package com;

import java.net.InetSocketAddress;
import java.util.HashSet;

public interface SuperPeer {

	public HashSet<FileInfo> searchFile(String query);
	public boolean downloadFile(InetSocketAddress dest,String checksum,int sessionID);
	boolean register(Peer p,boolean status);
	public boolean fileChanged(String nick, String fileName, long fileSize, String strfi, int stat);
	public FileInfo getFileInfo(String checksum);
}
