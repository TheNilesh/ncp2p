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
	@XmlElement(name="Shared-Directory")
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
		
		superPeers.add(new Host("127.0.0.1",1025));
		superPeers.add(new Host("127.0.0.1",4689));
		
		stunServers.add(new Host("127.0.0.1",4690));

		nick="Nilesh";
		sharedDirectory="C:\\Users\\Public\\Pictures";
	}

	public static Configuration getConf(String xmlFile){
		 try {
				File file = new File(xmlFile);
				if(!file.exists()){
					Configuration conf=new Configuration();
					conf.setSource(file.getAbsolutePath());
					conf.saveConf();
					return conf;
				}
				
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


