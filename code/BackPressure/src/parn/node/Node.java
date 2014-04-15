package parn.node;

import java.net.Inet4Address;
import java.net.InetAddress;

import parn.main.Main;

public class Node {
	public int id;
	public InetAddress address;
	
	//Note: not being used as of now. The Main explicitly maintains a HashMap<Integer, ShadowQueue>. Reason - fast creation of control packets.
	public int shadowQueue;
	// tokenBuckets;
	
	
	public Node(int id, InetAddress address) {
		super();
		this.id = id;
		this.address = address;
		shadowQueue=0;
	}
	
	public String toString(){
		return "Node["+id+"]";
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public InetAddress getAddress() {
		return address;
	}
	public void setAddress(Inet4Address address) {
		this.address = address;
	}
	/*public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}*/
	
	
}
