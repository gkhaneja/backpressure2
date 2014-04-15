package parn.packet;

import java.io.Serializable;

public class ShadowQueue implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4775788475760834895L;
	
	public int destination;
	public int length;
	
	public ShadowQueue(int destination, int length){
		this.destination = destination;
		this.length = length;
	}
	
	public void update(int change){
		length += change; 
	}
	
	public String toString(){
		return destination + "->" + length;
	}
}
