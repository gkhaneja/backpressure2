package parn.main;

import java.io.IOException;

import parn.packet.DataPacket;

public class FlowStat {
	
	//flow info
	public int flowId=-1;
	public int source;
	public int destination;
	
	//counts
	public int nPackets;
	public long nBytes;
	public long nPayloadBytes;
	
	//averages
	public double pathLength;
	public long travelTime;
	
	public int maxPathLength;
	public long maxTravelTime;
	
	public FlowStat(DataPacket packet){
		this.flowId = packet.flowID;
		this.source = packet.source;
		this.destination = packet.destination;
		
		try {
			this.nBytes = Main.sizeof(packet);
			this.nPayloadBytes = packet.payload.length;
			this.pathLength = packet.path.size() - 1;
			this.maxPathLength = packet.path.size() - 1;
			this.travelTime = System.currentTimeMillis() - packet.time;
			this.maxTravelTime = this.travelTime;
			this.nPackets = 1;
		} catch (IOException e) {
			System.out.println("ERROR: couldn't coount " + packet);
			this.nBytes=0;
			this.nPayloadBytes=0;
			this.pathLength=0;
			this.travelTime=0;
			this.nPackets=0;
		}
		
		
	}
	
	public void addPacket(DataPacket packet){
		if(packet.flowID != flowId){
			System.out.println("ERROR: Bad " + packet);
			return;
		}
		
		try {
			this.nBytes += Main.sizeof(packet);
			this.nPayloadBytes += packet.payload.length;
			this.pathLength = (this.nPackets*this.pathLength + 1.0 * (packet.path.size()-1)) / (this.nPackets+1);
			if(packet.path.size()-1 > this.maxPathLength){
				maxPathLength = packet.path.size()-1;
			}
			long currTravelTime = (System.currentTimeMillis() - packet.time);
			this.travelTime = (this.nPackets*this.travelTime + (currTravelTime))/(this.nPackets + 1);
			if(currTravelTime > this.maxTravelTime){
				this.maxTravelTime = currTravelTime;
			}
			this.nPackets++;
			//System.out.println("Data packet sizes: " + Main.sizeof(packet) + ", " + packet.payload.length);
		} catch (IOException e) {
			System.out.println("ERROR: couldn't coount " + packet);
		}
		
	}
	
	public String toString(){
		String ret = "STAT: [";
		ret += "flowId:" + flowId + ", ";
		ret += "source:" + source + ", ";
		ret += "dest:" + destination + ", ";
		ret += "nPackets:" + nPackets + ", ";
		ret += "nBytes:" + nBytes + ", ";
		ret += "nPayLoadBytes:" + nPayloadBytes + ", ";
		ret += "avgPathLength:" + pathLength + ", ";
		ret += "maxPathLength:" + maxPathLength + ", ";
		ret += "avgTravelTime:" + travelTime + ", ";
		ret += "maxTravelTime:" + maxTravelTime + "]";
		return ret;
	}
	
}
