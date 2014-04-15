package parn.worker;

import java.util.Iterator;

import parn.main.Main;
import parn.node.Neighbor;
import parn.packet.DataPacket;

public class Router extends Thread {
	
	public void run(){
		while(true){
			//TODO: parametrized sleep time for router
			try{
				sleep(5000);
			}catch(InterruptedException e){
				System.out.println(this + " got interrupted");
			}
			
			DataPacket packet = Main.inputBuffer.poll();
			//TODO: check
			if(packet==null) continue;
			//TODO: TokenBucket algorithm
			//TODO: the following meant to be testing snippet for thread working. IT should be removed and replaced by Tocket Bucket algorithm
			if(packet.destination == Main.ID){
				consumePacket();
			} else if(Main.nodes.containsKey(packet.destination)){
				//TODO: to be changed - 
				Iterator<Integer> it = Main.neighbors.keySet().iterator();
				Neighbor neighbor = Main.neighbors.get(it.next());
				try {
					neighbor.realQueue.put(packet);
				} catch (InterruptedException e) {
					System.out.println(this + ": Error transferring " + packet);
					//TODO: comment stack trace, may be
					e.printStackTrace();
				}
			}else{
				System.out.println("Unknown destination. Dropping " + packet);
			}
			//Flight to champaign - H3 gate 
			
			
			
		}
	}
	
	public void consumePacket(){
		//TODO: consume it with slat n pepper
	}
	
	public String toString(){
		return "Router";
	}

}
