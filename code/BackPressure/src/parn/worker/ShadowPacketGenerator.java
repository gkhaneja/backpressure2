package parn.worker;

import java.util.HashMap;
import java.util.Iterator;

import javax.sound.midi.Receiver;

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
			
			//Wait for the correct phase
			synchronized(Main.iterationPhaseLock){
				while(Main.iterationPhase==1){
					try {
						Main.iterationPhaseLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}						
			}
			
		
			
			//Do a check. If program is correct, this check should always pass
			if(Main.nShadowQueueReceived == Main.neighbors.size()){
				System.out.println("CONTROL: ERROR: " + this + " proper number of shadow queues were not received.");
			}
			
			try{
				sleep(Configurations.CONTROL_INTERVAL);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			iteration++;
			Main.iteration++;
			System.out.println("CONTROL: " + this + " Starting iteration " + Main.iteration + "," + Main.iterationPhase + "       time:  " + (System.currentTimeMillis() - Main.startTime));
			
			
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
				
				String printStr="";
				HashMap<Integer, Integer> shadowPackets = new HashMap<Integer, Integer>();
				if(maxWeight>0){
					//TODO: how many shadow packets are to be transfered over link: Capacity ?							
					int nShadowPackets = Main.Capacity;
					
					if(Configurations.DEBUG_ON){
						nShadowPackets = (int) ((System.currentTimeMillis() - time) / Configurations.SLOW_DOWN_FACTOR);
						printStr += "\tCONTROL: " + this + " DEBUG MODE: "  + nShadowPackets + " generated for  " + winnerDest;
					}else{
						nShadowPackets = (int) ((System.currentTimeMillis() - time)* Main.bandwidth / (2*Main.neighbors.size() * Main.dataPacketSize));
						printStr += "\tCONTROL: " + this + " REAL MODE: "  + nShadowPackets + " generated for  " + winnerDest;
					}
					//TODO: lock for shadow queue
					nShadowPackets = Main.shadowQueues.get(winnerDest).update(-1*nShadowPackets);
					printStr += ". Actual Shadow Packets sent: " + nShadowPackets;
					Main.shadowPacketsSent += nShadowPackets;

					Node winnerNode = Main.nodes.get(winnerDest);
					winnerNode.updateTokenBucket(neighbor.node.id, -1*nShadowPackets);
					shadowPackets.put(winnerDest, nShadowPackets);
					printStr += ". Sending " + nShadowPackets + " ShadowPackets to " + neighbor;
				}else{

					printStr += "CONTROL: " + this + " No shadow packets for " + neighbor;
				}
				System.out.println(printStr);
				neighbor.sendShadowPackets(shadowPackets, iteration);
			}
		
			

			
			//Reset neighbors SQ
			Iterator<Neighbor> iterator2 = Main.neighbors.values().iterator();
			while(iterator2.hasNext()){
				iterator2.next().reset();
			}
			System.out.println("CONTROL: " + this + " iteration ended");


			/*synchronized(Main.receivedShadowPacketLock){
						Main.receivedShadowPacketLock.notifyAll();
					}*/

			
			//Stability Check
			//TODO: Take the stability check module to somewhere else ?
			if(checkStability()){
				if(!Configurations.isStable){
					System.out.println("STAT: CONTROL: System is stable");
					Configurations.isStable = true;
					Configurations.stableIterations = iteration;
					Configurations.stableTime = System.currentTimeMillis() - Configurations.startTime;
				}else{
					System.out.println("STAT: System remains stable");
				}
			}else{
				if(!Configurations.isStable){
					System.out.println("STAT: System is NOT stable yet");
				}else{
					System.out.println("STAT: ERROR: System went from stable to unstable");
					Main.error = true;
				}
			}

			time = System.currentTimeMillis();
			
			synchronized(Main.receivedShadowPacketLock){
				while(Main.nShadowPacketReceived != Main.neighbors.size()){
					try{
						Main.receivedShadowPacketLock.wait();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				System.out.println("CONTROL: " + this + " Received all Shadow Packets");
				Main.nShadowPacketReceived=0;
				
				synchronized(Main.iterationPhaseLock){
					//System.out.println("CONTROL: " + this + " Starting " + Main.iteration + ", " + Main.iterationPhase);
					Main.iterationPhase=1;
					Main.iterationPhaseLock.notifyAll();					
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
