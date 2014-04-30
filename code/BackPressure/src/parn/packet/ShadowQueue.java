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
	
	public int update(int change){
		
		//TODO: Not sure about this check
		if(change < 0){
			if(change + length < 0){
				//System.out.println("ERROR: " + toString2());
				int temp = length;
				length=0;
				return temp;
			}else{
				length += change;
				return -1*change;
			}
			
		}
		
		length += change; 
		return change;
	}
	
	public String toString(){
		return destination + "->" + length;
	}
	
	public String toString2(){
		return "ShadowQueue[" + destination + "->" + length + "]";
	}
}
