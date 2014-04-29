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
		return "ShadowPacketGenerator[" + iteration + "]: "; 
	}
	
	public void run(){
		iteration=0;
		
		//Loop - check if Main.nShadowPacketReceived == #neighbor, then, Apply BackPressure algorithm: 
		//Calculate shadow packets, Update shadow queue, and send shadow packets to neighbors, followed by shadow queue
		while(!Configurations.SYSTEM_HALT){
            
			
			synchronized(Main.receivedShadowQueueLock){
				
				try {
					Main.receivedShadowQueueLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(Main.nShadowQueueReceived == Main.neighbors.size()){
					iteration++;
					
					System.out.println("CONTROL: " + this + "       time:  " + (System.currentTimeMillis() - Main.startTime));
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
						
						HashMap<Integer, Integer> shadowPackets = new HashMap<Integer, Integer>();
						if(maxWeight>0){
							//TODO: how many shadow packets are to be transfered over link: Capacity ?							
							int nShadowPackets = Main.Capacity;
							if(Configurations.DEBUG_ON){
								nShadowPackets = (int) ((System.currentTimeMillis() - time) / Configurations.SLOW_DOWN_FACTOR);
								System.out.print("\tCONTROL: " + this + " DEBUG MODE: "  + nShadowPackets + " generated for  " + winnerDest);
							}else{
								nShadowPackets = (int) ((System.currentTimeMillis() - time)* Main.bandwidth / (2*Main.neighbors.size() * Main.dataPacketSize));
								System.out.print("\tCONTROL: " + this + " REAL MODE: "  + nShadowPackets + " generated for  " + winnerDest);
							}
							//TODO: lock for shadow queue
							nShadowPackets = Main.shadowQueues.get(winnerDest).update(-1*nShadowPackets);
							System.out.print(". Actual Shadow Packets sent: " + nShadowPackets);
							Main.shadowPacketsSent += nShadowPackets;
							
							Node winnerNode = Main.nodes.get(winnerDest);
							winnerNode.updateTokenBucket(neighbor.node.id, -1*nShadowPackets);
							shadowPackets.put(winnerDest, nShadowPackets);
							System.out.println(". Sending " + nShadowPackets + " ShadowPackets to " + neighbor);
						}else{
							
							System.out.print("CONTROL: " + this + " No shadow packets for " + neighbor);
						}
							
							
						neighbor.sendShadowPackets(shadowPackets, iteration);
							
						
						
					}
					time = System.currentTimeMillis();
					System.out.println();
					
					//Main.reset();
					Main.nShadowQueueReceived=0;
					Iterator<Neighbor> iterator2 = Main.neighbors.values().iterator();
					while(iterator2.hasNext()){
						iterator2.next().reset();
					}
					System.out.println("CONTROL: " + this + " iteration ended");
					
					
					/*synchronized(Main.receivedShadowPacketLock){
						Main.receivedShadowPacketLock.notifyAll();
					}*/
					
					
					//TODO: check if probabilities are stabilized ?
					if(checkStability()){
						if(!Configurations.isStable){
							System.out.println("CONTROL: System is stable");
							Configurations.isStable = true;
							Configurations.stableIterations = iteration;
							Configurations.stableTime = System.currentTimeMillis() - Configurations.startTime;
						}else{
							System.out.println("System remains stable");
						}
						
					}else{
						if(!Configurations.isStable){
							System.out.println("System is NOT stable yet");
						}else{
							System.out.println("ERROR: System went from stable to unstable");
							Main.error = true;
						}
					}
					
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
