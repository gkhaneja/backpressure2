package parn.node;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;

import parn.main.Configurations;
import parn.main.Main;

public class Node {
	public int id;
	public InetAddress address;
	public int pathLength;
	
	//Note: not being used as of now. The Main explicitly maintains a HashMap<Integer, ShadowQueue>. Reason - fast creation of control packets.
	public int shadowQueue;
	// tokenBuckets;
	private HashMap<Integer, Integer> tokenBuckets;
	
	//packet counts
	public HashMap<Integer, Integer> packetsPerLink;
	public int packetsTotal;
	
	//ratios
	HashMap<Integer, Double> probs;
	HashMap<Integer, Double> prevProbs;
	
	
	
	public Node(int id, InetAddress address) {
		super();
		this.id = id;
		this.address = address;
		shadowQueue=0;
		tokenBuckets = new HashMap<Integer, Integer>();
		packetsTotal = 0;
		packetsPerLink = new HashMap<Integer, Integer>();
		probs = new HashMap<Integer, Double>();
		prevProbs = new HashMap<Integer, Double>();
	}
	
	public Node(int id, InetAddress address, int pathLength) {
		super();
		this.id = id;
		this.address = address;
		this.pathLength = pathLength;
		shadowQueue=0;
		tokenBuckets = new HashMap<Integer, Integer>();
		packetsTotal = 0;
		packetsPerLink = new HashMap<Integer, Integer>();
		probs = new HashMap<Integer, Double>();
		prevProbs = new HashMap<Integer, Double>();
	}
	
	public void init(){
		Iterator<Integer> iterator = Main.neighbors.keySet().iterator();
		while(iterator.hasNext()){
			int neighbor = iterator.next();
			tokenBuckets.put(neighbor, 0);
			packetsPerLink.put(neighbor, 0);
			probs.put(neighbor, 0.0);
			prevProbs.put(neighbor, 0.0);
			
		}
	}
	
	public boolean updateProbs(){
		Iterator<Integer> iterator = packetsPerLink.keySet().iterator();
		while(iterator.hasNext()){
			int neighbor = iterator.next();
			double oldProb = probs.get(neighbor);
			prevProbs.put(neighbor, oldProb);
			double newProb=0;
			if(packetsTotal!=0){
				newProb = (packetsPerLink.get(neighbor)*1.0) / (1.0*packetsTotal);
			}
			probs.put(neighbor, newProb);
			//System.out.println("old - " + oldProb + ". new - " + newProb);
		}
		//printProbs();
		return checkStability();
	}
	
	public void printProbs(){
		String ret = Configurations.hashToString(packetsPerLink);
		System.out.println(this + ": " + ret);
	}
	
	public boolean checkStability(){
		if(packetsTotal==0 && id!=Main.ID){
			return false;
		}
		Iterator<Integer> iterator = packetsPerLink.keySet().iterator();
		while(iterator.hasNext()){
			int neighbor = iterator.next();
			//System.out.println("Stability check: " + probs.get(neighbor) + " " + prevProbs.get(neighbor));
			if(Math.abs(probs.get(neighbor) - prevProbs.get(neighbor)) >= Main.stabilityDiff){
				return false;
			}
		}
		return true;
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
		packetsPerLink.put(smallestTokenBucket, packetsPerLink.get(smallestTokenBucket) + 1);
		packetsTotal++;
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
