package parn.packet;

import java.io.Serializable;
import java.util.ArrayList;

import parn.main.Configurations;

public class PacketBatch implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3794672253721263606L;
	/**
	 *  
	 */
	
	
	public int flowID;
	public int source;
	int ttl;
	int hops;
	public int destination;
	public int startSequenceNumber;
	public int endSequenceNumber;
	//public ArrayList<Character> path;
	//public long time;
	//public byte[] payload;
	
	
	public PacketBatch(int flowID, int source, int destination, int startSequenceNumber, int endSequenceNumber){
		this.flowID = flowID;
		this.source = source;
		this.destination = destination;
		//this.ttl = Configurations.MAX_HOP;
		//this.hops=0;
		this.startSequenceNumber = startSequenceNumber;
		this.endSequenceNumber = endSequenceNumber;
		//path = new ArrayList<Character>();
		//time = System.currentTimeMillis();
		//TODO: I think arrays are initialized by default. Confirm this by measuring size.
		//payload = new byte[Configurations.PAYLOAD_SIZE];
	}

	public PacketBatch(int flowID, int source, int destination, int startSequenceNumber, int endSequenceNumber, int payloadSize){
		this.flowID = flowID;
		this.source = source;
		this.destination = destination;
		this.ttl = Configurations.MAX_HOP;
		this.hops=0;
		this.startSequenceNumber = startSequenceNumber;
		this.endSequenceNumber = endSequenceNumber;
		//path = new ArrayList<Character>();
		//time = System.currentTimeMillis();
		//payload = new byte[payloadSize];
	}

	
	/*public void decrementTTL(){
		ttl--;
	}*/
	
	
	
	public String toString(){
		return "DataPackets["  + flowID + ", " + source + ", " + destination + ", " + startSequenceNumber + ", " + endSequenceNumber + "]";
	}
}
