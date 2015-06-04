package peer;

public class NotConnectedException extends Exception {

	private static final long serialVersionUID = 1L;
	String desc;
	public NotConnectedException(){
		desc="SuperPeer not connected";
	}
	
	public String toString(){
		return desc;
	}
	public void printStackTrace(){
		System.out.println(desc);
	}
}
