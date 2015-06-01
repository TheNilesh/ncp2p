package com;

import java.io.File;
import java.util.LinkedList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Configuration {

	String xmlFile;
	
	@XmlElement(name="Nick-Name")
	String nick;
	@XmlElement(name="Shared-Sirectory")
	String sharedDirectory;

	@XmlElementWrapper(name = "superpeerlist")
	@XmlElement(name = "SuperPeer")
	LinkedList<Host> superPeers;
	
	@XmlElementWrapper(name = "stunserverlist")
	@XmlElement(name = "STUNServer")
	LinkedList<Host> stunServers;

	private Configuration(){
		superPeers=new LinkedList<Host>();
		stunServers=new LinkedList<Host>();
		
		superPeers.add(new Host("192.168.2.125",1025));
		superPeers.add(new Host("192.168.2.125",1025));
		
		stunServers.add(new Host("192.168.2.125",1025));
		stunServers.add(new Host("192.168.2.125",1025));

		nick="Nilesh";
		sharedDirectory="C:\\Users\\Public";
	}

	public static Configuration getConf(String xmlFile){
		 try {
				File file = new File(xmlFile);
				JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
		 
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				Configuration conf = (Configuration) jaxbUnmarshaller.unmarshal(file);
				conf.setSource(xmlFile);
				
				return conf;
			  } catch (JAXBException e) {
				e.printStackTrace();
			  }
		 return null;
	}
	
	private void setSource(String xmlFile2) {
		xmlFile=xmlFile2;
	}

	public void saveConf(){
		  try {

		        File file = new File(xmlFile);
		        
		        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
		        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		        jaxbMarshaller.marshal(this, file);
		     //   jaxbMarshaller.marshal(this, System.out);

		        } catch (JAXBException e) {
		        	 e.printStackTrace();
		        }
	}
	
	public static void main(String args[]){
		Configuration conf=new Configuration(); //create
		conf.setSource("E:\\abc.xml");
		conf.saveConf();	//save
	}//main

	public LinkedList<Host> getSuperpeers() {
		return superPeers;
	}
	
	public LinkedList<Host> getStuns(){
		return stunServers;
	}

	public String getNick() {
		return nick;
	}
	
	public String getSharedDir(){
		return sharedDirectory;
	}

	public void setPeerName(String text) {
		nick=text;
	}

	public void setShareDir(String text) {
		sharedDirectory=text;
	}

	public String getSource() {
		return xmlFile;
	}
}


