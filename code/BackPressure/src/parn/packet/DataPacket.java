package parn.packet;

import java.io.Serializable;
import java.util.ArrayList;

import parn.main.Configurations;

public class DataPacket implements Serializable {
	/**
	 *  
	 */
	private static final long serialVersionUID = 7219908023845170484L;
	
	public int flowID;
	public int source;
	int ttl;
	int hops;
	public int destination;
	public int sequenceNumber;
	public ArrayList<Integer> path;
	long time;
	byte[] payload;
	
	
	public DataPacket(int flowID, int source, int destination, int sequenceNumber){
		this.flowID = flowID;
		this.source = source;
		this.destination = destination;
		this.ttl = Configurations.MAX_HOP;
		this.hops=0;
		this.sequenceNumber = sequenceNumber;
		path = new ArrayList<Integer>();
		time = System.currentTimeMillis();
		payload = new byte[Configurations.PAYLOAD_SIZE];
	}

	public DataPacket(int flowID, int source, int destination, int sequenceNumber, int payloadSize){
		this.flowID = flowID;
		this.source = source;
		this.destination = destination;
		this.ttl = Configurations.MAX_HOP;
		this.hops=0;
		this.sequenceNumber = sequenceNumber;
		path = new ArrayList<Integer>();
		time = System.currentTimeMillis();
		payload = new byte[payloadSize];
	}

	
	/*public void decrementTTL(){
		ttl--;
	}*/
	
	public boolean checkValidity(){
		if(ttl<0) {
			System.out.println(this + " is being dropped");
			return false;
		}
		ttl--;
		hops++;
		return true;
	}
	
	public String toString(){
		return "DataPacket["  + flowID + ", " + source + ", " + destination + ", " + ttl + ", " + hops + "]";
	}
}
