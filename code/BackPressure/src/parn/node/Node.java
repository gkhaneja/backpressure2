package parn.node;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;

import parn.main.Main;

public class Node {
	public int id;
	public InetAddress address;
	
	//Note: not being used as of now. The Main explicitly maintains a HashMap<Integer, ShadowQueue>. Reason - fast creation of control packets.
	public int shadowQueue;
	// tokenBuckets;
	private HashMap<Integer, Integer> tokenBuckets;
	
	
	
	public Node(int id, InetAddress address) {
		super();
		this.id = id;
		this.address = address;
		shadowQueue=0;
		tokenBuckets = new HashMap<Integer, Integer>();
		
	}
	
	public void init(){
		Iterator<Integer> iterator = Main.neighbors.keySet().iterator();
		while(iterator.hasNext()){
			tokenBuckets.put(iterator.next(), 0);
		}
	}
	
	public int getTokenBucket(){
		int smallestTokenBucket=-1;
		int smallestTokenBucketValue=0;
		Iterator<Integer> iterator = tokenBuckets.keySet().iterator();
		while(iterator.hasNext()){
			int tokenBucket = iterator.next();
			int tokenBucketValue = tokenBuckets.get(tokenBucket);
			if(tokenBucketValue <= smallestTokenBucketValue){
				smallestTokenBucket = tokenBucket;
				smallestTokenBucketValue = tokenBucketValue;
			}
		}
		return smallestTokenBucket;
	}
	
	public void updateTokenBucket(int link, int change){
		//TODO: lock ?
		if(!tokenBuckets.containsKey(link)){
			tokenBuckets.put(link, 0);
			System.out.println("WARN: Missing token bucket " +  link + " from " + this);
		}
		int tokenValue = tokenBuckets.get(link);
		int newTokenValue = (tokenValue + change < 0) ? 0 : tokenValue + change;
		tokenBuckets.put(link, newTokenValue);
		
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
