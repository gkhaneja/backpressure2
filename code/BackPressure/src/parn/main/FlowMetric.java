package parn.main;

import parn.packet.DataPacket;

public class FlowMetric {
	
	//flow info
	public int flowId;
	public int source;
	public int destination;
	
	//counts
	public int nPackets;
	public long nBytes;
	public long nPayloadBytes;
	
	//averages
	public int pathLength;
	public long travelTime;
	
	public FlowMetric(DataPacket packet){
		this.flowId = packet.flowID;
		this.source = packet.source;
		this.destination = packet.destination;
		this.nPackets = 1;
		//this.nBytes = 
	}
	
	
}
