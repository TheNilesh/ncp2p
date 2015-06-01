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
				System.out.println(conf);
				return conf;
			  } catch (JAXBException e) {
				e.printStackTrace();
			  }
		 return null;
	}
	
	public void setConf(String xmlFile){
		  try {

		        File file = new File(xmlFile);
		        
		        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
		        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		        // output pretty printed
		        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		        jaxbMarshaller.marshal(this, file);
		        
		        jaxbMarshaller.marshal(this, System.out);

		        } catch (JAXBException e) {
		        	 e.printStackTrace();
		        }
	}
	
	public static void main(String args[]){
		Configuration conf=new Configuration();
		conf.setConf("C:\\a.xml");
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
}


