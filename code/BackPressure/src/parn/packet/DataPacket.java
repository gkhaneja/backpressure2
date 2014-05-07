package parn.packet;

import java.io.Serializable;
import java.util.ArrayList;

import parn.main.Configurations;
import parn.main.Main;

public class DataPacket implements Serializable {
	/**
	 *  
	 */
	private static final long serialVersionUID = 7219908023845170484L;
	
	public boolean batch=false;
	
	public int flowID;
	public int source;
	public int destination;
	public long time;
	int ttl;
	int hops;	
	public ArrayList<Character> path;
	
	
	public int sequenceNumber;
	public byte[] payload;
	
	public int startSequenceNumber;
	public int endSequenceNumber;
	
	
	
	
	public DataPacket(int flowID, int source, int destination, int sequenceNumber){
		this.flowID = flowID;
		this.source = source;
		this.destination = destination;
		this.ttl = Configurations.MAX_HOP;
		this.hops=0;
		this.sequenceNumber = sequenceNumber;
		path = new ArrayList<Character>();
		time = System.currentTimeMillis();
		payload = new byte[Configurations.PAYLOAD_SIZE];
	}

	/*public DataPacket(int flowID, int source, int destination, int sequenceNumber, int payloadSize){
		this.flowID = flowID;
		this.source = source;
		this.destination = destination;
		this.ttl = Configurations.MAX_HOP;
		this.hops=0;
		this.sequenceNumber = sequenceNumber;
		path = new ArrayList<Character>();
		time = System.currentTimeMillis();
		payload = new byte[payloadSize];
	}*/

	
	public DataPacket(int flowID, int source, int destination, int startSequenceNumber, int endSequenceNumber, boolean batch){
		this.batch=true;
		this.flowID = flowID;
		this.source = source;
		this.destination = destination;
		this.ttl = Configurations.MAX_HOP;
		this.hops=0;
		this.startSequenceNumber = startSequenceNumber;
		this.endSequenceNumber = endSequenceNumber;
		path = new ArrayList<Character>();
		time = System.currentTimeMillis();
		
	}
	
	/*public void decrementTTL(){
		ttl--;
	}*/
	
	public boolean checkValidity(){
		if(ttl<0) {
			if(Main.verbose){
				System.out.println(this + " is being dropped");
			}
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
