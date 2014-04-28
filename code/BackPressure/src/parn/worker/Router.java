package parn.worker;

import parn.main.Configurations;
import parn.main.FlowStat;
import parn.main.Main;
import parn.node.Neighbor;
import parn.node.Node;
import parn.packet.DataPacket;

public class Router extends Thread {
	
	public void run(){
		while(!Configurations.SYSTEM_HALT){
			if(Configurations.DEBUG_ON){
				try{
					sleep(Configurations.SLOW_DOWN_FACTOR);
				}catch(InterruptedException e){
					System.out.println(this + " got interrupted");
				}
			}
			
			DataPacket packet = Main.inputBuffer.poll();
			if(packet==null) continue;
			//TokenBucket algorithm
			packet.path.add((char) Main.ID);	
			Main.dataPacketsReceived++;
			if(packet.destination == Main.ID){
				consumePacket(packet);
			} else if(Main.nodes.containsKey(packet.destination)){
				Node destination = Main.nodes.get(packet.destination);
				int link = destination.getTokenBucket();
				
				Neighbor neighbor = Main.neighbors.get(link);
				System.out.println("Router: routing " + packet + " to " + neighbor);
				try {
					Main.dataPacketsSent++;
					neighbor.realQueue.put(packet);
				} catch (InterruptedException e) {
					System.out.println(this + ": Error transferring " + packet);
					e.printStackTrace();
				}
			}else{
				System.out.println("Unknown destination. Dropping " + packet);
			}
			
			
			
			
		}
	}
	
	public void consumePacket(DataPacket packet){
		//TODO: Assuming at most one flow source
		System.out.println("Consuming " + packet);
		if(Main.flowStatReceived.containsKey(packet.source)){
			Main.flowStatReceived.get(packet.source).addPacket(packet);
		}else{
			Main.flowStatReceived.put(packet.source, new FlowStat(packet));
		}
	}
	
	public String toString(){
		return "Router";
	}

}
