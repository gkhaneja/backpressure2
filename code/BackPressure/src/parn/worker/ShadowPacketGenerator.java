package parn.worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import parn.main.Configurations;
import parn.main.Main;
import parn.node.Neighbor;
import parn.node.Node;
import parn.packet.ShadowPacket;

public class ShadowPacketGenerator extends Thread {

	public int iteration;
	public long time=System.currentTimeMillis();
	Random rand = new Random();
	double maxRate=0;

	public String toString(){
		return "ShadowPacketGenerator[" + iteration + "]: "; 
	}

	@SuppressWarnings("unchecked")
	public void run(){
		iteration=0;
		try{
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

				//if(Configurations.isStable){
				
					sleep(Configurations.CONTROL_INTERVAL);
				
				//}

				iteration++;
				Main.iteration++;
				if(Main.verbose){
					System.out.println("CONTROL: " + this + " Starting iteration " + Main.iteration + "," + Main.iterationPhase + "       time:  " + (System.currentTimeMillis() - Main.startTime));
				}

				String printStr="";
				Iterator<Integer> iterator = Main.neighbors.keySet().iterator();
				long thisTime = System.currentTimeMillis();
				
				
				while(iterator.hasNext()){
					Neighbor neighbor = Main.neighbors.get(iterator.next());
					Iterator<Integer> destinations = neighbor.shadowQueues.keySet().iterator();
					ArrayList<ShadowPacket> weighs = new ArrayList<ShadowPacket>();

					int maxWeight = 0;
					int winnerDest=-1;
					while(destinations.hasNext()){
						int dest = destinations.next();
						//System.out.print( " " + Main.shadowQueues.get(dest).length + "-" + neighbor.shadowQueues.get(dest).length + "-" + Main.M);
						//int MLocal = (int) (Main.M*Router.packetRate3 / Main.neighbors.size());
						int MLocal = (int) (Main.M * Configurations.PACKET_RATE );
						//System.out.println("CONTROL: MLocal: " + MLocal + " packetRate: " + Router.packetRate3);
						int weight = Main.shadowQueues.get(dest).length - neighbor.shadowQueues.get(dest).length - MLocal;
						
						if(Main.useNeighborOp==1){
							if(( weight > 0 && dest == neighbor.node.id) || (!(Main.neighbors.containsKey(dest)) && weight > 0)){
								weighs.add(new ShadowPacket(dest, weight));

							}
						}else{
							if(weight > 0 ){
								weighs.add(new ShadowPacket(dest, weight));

							}
						}
						if((weight > maxWeight) || (weight == maxWeight && rand.nextBoolean())){
							winnerDest = dest;
							maxWeight  = weight;
						}
					}

					//String printStr="";
					HashMap<Integer, Integer> shadowPackets = new HashMap<Integer, Integer>();
					int totalPackets=0;

					//prop. splitting
					if(Main.usePropSplitting==1){
						//System.out.println("\tCONTROL: " + this + " Proportional splitting is ON");
						if(weighs.size() > 0 ){
							long shadowPacketsLimit=1;
							if(Router.packetRate4 >= maxRate){
								maxRate = Router.packetRate4;
								
							}
							if(Main.verbose){
								System.out.println("PLEASE: " + Router.packetRate4 + " maxRate: " + maxRate);
							}
							double part1 = Main.shadowPaketRateFactor * 1.0*maxRate / (1.0*Main.neighbors.size());
							double part2 = 1.0*(thisTime - time) / 1000.0;
							shadowPacketsLimit = (long) (part1 * part2);
							printStr += "\tCONTROL: " + this + " limit: "  + shadowPacketsLimit;

							Collections.sort(weighs);
							int topK = Main.TOP_K;
							if(topK==-1){
								topK = Main.nodes.size();
							}

							//Calculate sum
							long sum=0;
							for(int i=weighs.size()-1; i>=0 && weighs.size() - i <=topK; i--){
								sum += weighs.get(i).nPackets;
							}

							//Calculate Ideal and actual shadow packet distribution
							HashMap<Integer, Integer> idealShadowPacketDist = new HashMap<Integer, Integer>();
							for(int i=weighs.size()-1; i>=0 && weighs.size() - i <=topK; i--){
								int dest = weighs.get(i).destination;
								int idealSP = (int) (shadowPacketsLimit * weighs.get(i).nPackets / sum);
								idealShadowPacketDist.put(dest, idealSP);
								int actualSP = Main.shadowQueues.get(dest).update(-1*idealSP);
								shadowPackets.put(dest, actualSP);

								synchronized(Main.lastLock){
									//System.out.println("This should not be null: " + Main.shdwSent.get(dest));
									//System.out.println("Neighbor: " + neighbor.node.id);
									HashMap<Integer, Integer> temp = Main.shdwSent.get(dest);
									temp.put(neighbor.node.id, temp.get(neighbor.node.id) + actualSP);
									//System.out.println(temp);
									Main.shdwSent.put(dest, temp);
									//System.out.println("Updated ShadowSent Stats");
								}
								
								Main.shadowPacketsSent += actualSP;
								totalPackets += actualSP;
								Node node = Main.nodes.get(dest);
								node.updateTokenBucket(neighbor.node.id, -1*actualSP);

							}
							printStr += ". Ideal: " + Configurations.hashToString(idealShadowPacketDist);
							printStr += ". Actual: " + Configurations.hashToString(shadowPackets);

						}else{
							printStr += "CONTROL: " + this + " No shadow packets for " + neighbor;
						}
					}else{
						/*if(maxWeight>0){
							int nShadowPackets=1;

							if(Configurations.DEBUG_ON){
								nShadowPackets = (int) ((System.currentTimeMillis() - time) / Configurations.SLOW_DOWN_FACTOR);
								printStr += "\tCONTROL: " + this + " DEBUG MODE: "  + nShadowPackets + " generated for  " + winnerDest;
							}else{
								System.out.println("CONTROL: currTime: " + System.currentTimeMillis() + ", prevIterationTime: " + time);

								//double part1 = 1.0*Main.bandwidth / (Main.dataPacketSize * 2.0*Main.neighbors.size());
								double part1 = Main.shadowPaketRateFactor * 1.0*Router.packetRate3 / (1.0*Main.neighbors.size());
								double part2 = 1.0*(System.currentTimeMillis() - time) / 1000.0;
								long nShadowPacketsTemp = (long) (part1 * part2);

								//long part1 = Main.bandwidth / Main.dataPacketSize ;
								//long nShadowPacketsTemp = ((System.currentTimeMillis() - time)* part1 / (2*Main.neighbors.size() * 1000));




								nShadowPackets = (int) nShadowPacketsTemp;
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
						}*/

					}
					if(Main.DEBUG){
						synchronized(Main.lastLock){
							Main.lastShadowPacketsSent += totalPackets;
						}
					}
					printStr += " QueueLength: " + neighbor.realQueue.size();
					neighbor.sendShadowPackets(shadowPackets, iteration);
				}

				if(Main.verbose){
					System.out.println(printStr);
				}


				//Reset neighbors SQ
				Iterator<Neighbor> iterator2 = Main.neighbors.values().iterator();
				while(iterator2.hasNext()){
					iterator2.next().reset();
				}
				if(Main.verbose){
					System.out.println("CONTROL: " + this + " iteration ended");
				}


				/*synchronized(Main.receivedShadowPacketLock){
						Main.receivedShadowPacketLock.notifyAll();
					}*/


				//Stability Check
				//TODO: Take the stability check module to somewhere else ?
				boolean stableRet = checkStability();
				if(stableRet){
					Main.stableCounter++;
				}else{
					Main.stableCounter=0;
				}
				if(Main.stableCounter >= 4){
					stableRet=true;
					if(!Configurations.isStable){
						if(Main.verbose){
							System.out.println("STAT: System is stable");
						}
						Configurations.isStable = true;
						Configurations.stableIterations = iteration;
						Configurations.stableTime = System.currentTimeMillis() - Configurations.startTime;
					}else{
						if(Main.verbose){
							System.out.println("STAT: System remains stable");
						}
					}
				}else{
					stableRet=false;
					if(!Configurations.isStable){
						if(Main.verbose){
							System.out.println("STAT: System is NOT stable yet");
						}
					}else{
						if(Main.verbose){
							System.out.println("STAT: ERROR: System went from stable to unstable");
						}
						Main.error = true;
					}
				}
				if(Main.DEBUG){
					//System.out.println("PROB: " + stableRet);
					//System.out.println("PROB: ");
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
					if(Main.verbose){
						System.out.println("CONTROL: " + this + " Received all Shadow Packets");
					}
					Main.nShadowPacketReceived=0;

					synchronized(Main.iterationPhaseLock){
						//System.out.println("CONTROL: " + this + " Starting " + Main.iteration + ", " + Main.iterationPhase);
						Main.iterationPhase=1;
						Main.iterationPhaseLock.notifyAll();					
					}
				}




			}
		}catch(Throwable e){
			e.printStackTrace();
			
			System.out.println(this + " FATAL ERROR " + e.getMessage() + " " + e.getLocalizedMessage() + " " + e.toString() + e.getStackTrace().toString());
			Configurations.FATAL_ERROR = true;
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
