package parn.worker;

import java.util.Iterator;

import parn.main.Main;
import parn.node.Neighbor;

public class ShadowPacketGenerator extends Thread {
	
	public void run(){
		//Loop - check if Main.nShadowPacketReceived == #neighbor, then, Apply BackPressure algorithm: 
		//Calculate shadow packets, Update shadow queue, and send shadow packets to neighbors, followed by shadow queue
		while(true){
			synchronized(Main.syncLock){
				if(Main.nShadowQueueReceived == Main.neighbors.size()){
					Iterator<Integer> iterator = Main.neighbors.keySet().iterator();
					while(iterator.hasNext()){
						Neighbor neighbor = Main.neighbors.get(iterator.next());
						if(neighbor)
					}
					
				}
				try {
					Main.syncLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}

}
