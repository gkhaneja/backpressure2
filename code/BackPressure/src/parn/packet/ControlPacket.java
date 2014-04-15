package parn.packet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import parn.main.Main;

public class ControlPacket implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5741671362624985474L;
	
	public int source;
	public int destination;
	public HashMap<Integer, ShadowQueue> shadowQueues;
	
	//TODO: what else ?

	public ControlPacket(int source, int destination){
		this.source=source;
		this.destination=destination;
		shadowQueues = Main.getShadowQueues();
	}
	
	public String toString(){
		String ret="ControlPacket[";
		Iterator<Integer> shadowQueueIterator = shadowQueues.keySet().iterator();
		while(shadowQueueIterator.hasNext()){
			ret += shadowQueues.get(shadowQueueIterator.next()) + ", ";
		}
		return ret + "]";
	}
	
	
}
