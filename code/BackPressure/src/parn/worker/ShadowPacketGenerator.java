package parn.worker;

import java.util.HashMap;
import java.util.Iterator;

import parn.main.Configurations;
import parn.main.Main;
import parn.node.Neighbor;
import parn.node.Node;

public class ShadowPacketGenerator extends Thread {
	
	public int iteration;
	public long time=System.currentTimeMillis();
	
	public String toString(){
		return "ShadowPacketGenerator[" + iteration + "]"; 
	}
	
	public void run(){
		iteration=0;
		
		//Loop - check if Main.nShadowPacketReceived == #neighbor, then, Apply BackPressure algorithm: 
		//Calculate shadow packets, Update shadow queue, and send shadow packets to neighbors, followed by shadow queue
		while(!Configurations.SYSTEM_HALT){
            System.out.println("INFO: " + Main.startTime + " " + Main.stopTime + " " + System.currentTimeMillis());
			  if (System.currentTimeMillis() - Main.startTime > Main.stopTime){
	                Configurations.SYSTEM_HALT = true;
	                System.out.println("INFO: Stopping the system");
	                continue;
	            }
			
			synchronized(Main.syncLock){
				if(Main.nShadowQueueReceived == Main.neighbors.size()){
					iteration++;
					
					System.out.print(this);
					Iterator<Integer> iterator = Main.neighbors.keySet().iterator();
					while(iterator.hasNext()){
						Neighbor neighbor = Main.neighbors.get(iterator.next());
						Iterator<Integer> destinations = neighbor.shadowQueues.keySet().iterator();
						int maxWeight = 0;
						int winnerDest=-1;
						while(destinations.hasNext()){
							int dest = destinations.next();
							//System.out.print( " " + Main.shadowQueues.get(dest).length + "-" + neighbor.shadowQueues.get(dest).length + "-" + Main.M);
							int weight = Main.shadowQueues.get(dest).length - neighbor.shadowQueues.get(dest).length - Main.M;
							if(weight > maxWeight){
								winnerDest = dest;
								maxWeight  = weight;
							}
						}
						if(maxWeight>0){
							//TODO: how many shadow packets are to be transfered over link: Capacity ?
							
							int nShadowPackets = Main.Capacity;
							if(Configurations.DEBUG_ON){
								nShadowPackets = (int) ((System.currentTimeMillis() - time) / Configurations.SLOW_DOWN_FACTOR);
								System.out.println("DEBUG MODE: #Shadow packets " + nShadowPackets);
							}else{
								nShadowPackets = (int) ((System.currentTimeMillis() - time)* Main.bandwidth / (2*Main.neighbors.size() * Main.dataPacketSize));
							}
							
							//TODO: lock for shadow queue
							nShadowPackets = Main.shadowQueues.get(winnerDest).update(-1*nShadowPackets);
							System.out.println("Actual Shadow Packets " + nShadowPackets);
							Main.shadowPacketsSent += nShadowPackets;
							HashMap<Integer, Integer> shadowPackets = new HashMap<Integer, Integer>();
							Node winnerNode = Main.nodes.get(winnerDest);
							winnerNode.updateTokenBucket(neighbor.node.id, -1*nShadowPackets);
							shadowPackets.put(winnerDest, nShadowPackets);
							System.out.print(" Sending " + nShadowPackets + " SahdowPackets to " + neighbor);
							neighbor.sendShadowPackets(shadowPackets);
							
						}else{
							System.out.print(" No packets for " + neighbor);
						}
						
					}
					time = System.currentTimeMillis();
					System.out.println();
					//TODO: send shadowQueues
					//set a sync variable (with locks), on which controlpacketsenders are waiting.
					//Done.
					synchronized(Main.shadowQueueSendingNotification){
						Main.shadowQueueSendingNotification.notifyAll();
					}
					Main.reset();
					
					//TODO: check if probabilities are stabilized ?
					if(checkStability()){
						if(!Configurations.isStable){
							System.out.println("INFO: System is stable");
							Configurations.isStable = true;
							Configurations.stableIterations = iteration;
							Configurations.stableTime = System.currentTimeMillis() - Configurations.startTime;
						}else{
							
						}
						
					}else{
						if(!Configurations.isStable){
							System.out.println("NOT stable yet");
						}else{
							System.out.println("ERROR: System went from stable to unstable");
						}
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
	
	
	
	public boolean checkStability(){
		Iterator<Integer> iterator = Main.nodes.keySet().iterator();
		boolean isStable = true;
		while(iterator.hasNext()){
			int node = iterator.next();
			if(!Main.nodes.get(node).updateProbs()){
				isStable=false;
			}
		}
		return isStable;
	}

}
