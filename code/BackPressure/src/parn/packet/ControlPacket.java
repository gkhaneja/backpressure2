package parn.packet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import parn.main.Configurations;
import parn.main.Main;

public class ControlPacket implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5741671362624985474L;
	
	public int source;
	public int destination;
	public int type;
	public HashMap<Integer, ShadowQueue> shadowQueues;
	public HashMap<Integer, Integer> shadowPackets;
	
	//TODO: Include more fields ?

	public ControlPacket(int source, int destination, int type){
		this.source=source;
		this.destination=destination;
		this.type = type;
		if(this.type==Configurations.SHADOW_QUEUE_TYPE){ 
			shadowQueues = Main.getShadowQueues();
			shadowPackets = null;
		}else{
			shadowQueues = null;
			shadowPackets = new HashMap<Integer, Integer>();
		}
		
	}
	
	public void addShadowPacket(int destination, int npackets){
		if(type==Configurations.SHADOW_PACKET_TYPE){
			shadowPackets.put(destination, npackets);
		}else{
			System.out.println("ERROR: " + this + " received addShadowPacket- " + destination + ", " + npackets);
		}
	}
	
	public String toString(){
		String ret="ControlPacket[";
		if(type==Configurations.SHADOW_QUEUE_TYPE){
			ret += "ShadowQueues][";
			Iterator<Integer> shadowQueueIterator = shadowQueues.keySet().iterator();
			while(shadowQueueIterator.hasNext()){
				ret += shadowQueues.get(shadowQueueIterator.next()) + ", ";
			}
		}else{
			ret += "ShadowPackets][";
			Iterator<Integer> shadowPacketsIterator = shadowPackets.keySet().iterator();
			while(shadowPacketsIterator.hasNext()){
				int destination = shadowPacketsIterator.next();
				ret += destination + "->" + shadowPackets.get(destination) + ", ";
			}
		}
		return ret + "]";
	}
	
	
}
