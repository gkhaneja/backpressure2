package parn.worker;

import java.util.Iterator;

import parn.main.Configurations;
import parn.main.Main;
import parn.node.Neighbor;
import parn.node.Node;
import parn.packet.DataPacket;

public class Router extends Thread {
	
	public void run(){
		while(true){
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
			packet.path.add(Main.ID);		
			if(packet.destination == Main.ID){
				consumePacket(packet);
			} else if(Main.nodes.containsKey(packet.destination)){
				Node destination = Main.nodes.get(packet.destination);
				int link = destination.getTokenBucket();
				
				Neighbor neighbor = Main.neighbors.get(link);
				System.out.println("Router: routing " + packet + " to " + neighbor);
				try {
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
		//TODO: consume packet, update end-to-end metrics
		System.out.println("Consuming " + packet);
	}
	
	public String toString(){
		return "Router";
	}

}
