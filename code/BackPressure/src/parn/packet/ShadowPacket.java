package parn.packet;

import java.util.Random;

public class ShadowPacket implements Comparable<Object> {
	public int destination;
	public int nPackets;
	public static Random rand = new Random();
	
	public ShadowPacket(int destination, int nPackets){
		this.destination = destination;
		this.nPackets = nPackets;
	}

	@Override
	public int compareTo(Object arg0) {
		ShadowPacket other = (ShadowPacket) arg0;
		
		if(other.nPackets < this.nPackets){
			return +1;
		}else if (other.nPackets > this.nPackets) {
			return -1;
		}else if(rand.nextBoolean()){
			return -1;
		}
		return +1;
	}
}
