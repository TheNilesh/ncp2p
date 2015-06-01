package com;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Host{
	public Host(){}
	public Host(String ip, int port) {
		this.ip=ip;
		this.port=port;
	}
	@XmlElement
	String ip;
	@XmlElement
	int port;
	
	public String getIp(){
		return ip;
	}
	
	public int getPort(){
		return port;
	}
}
