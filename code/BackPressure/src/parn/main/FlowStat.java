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
	public int pathLength;
	public long travelTime;
	
	public FlowStat(DataPacket packet){
		this.flowId = packet.flowID;
		this.source = packet.source;
		this.destination = packet.destination;
		
		try {
			this.nBytes = Main.sizeof(packet);
			this.nPayloadBytes = packet.payload.length;
			this.pathLength = packet.path.size();
			this.travelTime = System.currentTimeMillis() - packet.time;
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
			this.pathLength = (this.nPackets*this.pathLength + packet.path.size()) / (this.nPackets+1);
			this.travelTime = (this.nPackets*this.travelTime + (System.currentTimeMillis() - packet.time))/(this.nPackets + 1);
			this.nPackets++;
		} catch (IOException e) {
			System.out.println("ERROR: couldn't coount " + packet);
		}
		
	}
	
	
}
