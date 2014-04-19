package parn.worker;

import java.util.HashMap;
import java.util.Iterator;

import parn.main.Main;
import parn.node.Neighbor;
import parn.node.Node;

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
						Iterator<Integer> destinations = neighbor.shadowQueues.keySet().iterator();
						int maxWeight = 0;
						int winnerDest=-1;
						while(destinations.hasNext()){
							int dest = destinations.next();
							int weight = Main.shadowQueues.get(dest).length - neighbor.shadowQueues.get(dest).length - Main.M;
							if(weight > maxWeight){
								winnerDest = dest;
								weight = maxWeight;
							}
						}
						if(maxWeight>0){
							//TODO: how many shadow packets are to be transfered over link: Capacity ?
							int nShadowPackets = Main.Capacity;
							//TODO: lock for shadow queue
							Main.shadowQueues.get(winnerDest).update(-1*nShadowPackets);
							HashMap<Integer, Integer> shadowPackets = new HashMap<Integer, Integer>();
							Node winnerNode = Main.nodes.get(winnerDest);
							winnerNode.updateTokenBucket(neighbor.node.id, -1*nShadowPackets);
							shadowPackets.put(winnerDest, nShadowPackets);
							neighbor.sendShadowPackets(shadowPackets);
							
						}
						
					}
					//TODO: send shadowQueues
					//set a sync variable (with locks), on which controlpacketsenders are waiting.
					//Done.
					Main.shadowQueueSendingNotification.notifyAll();
					Main.reset();
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
