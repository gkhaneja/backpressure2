package parn.worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import parn.main.Configurations;
import parn.main.FlowStat;
import parn.main.Main;
import parn.node.Neighbor;
import parn.node.Node;
import parn.packet.DataPacket;

public class Router extends Thread {

	
	
	int batchCount = 0;
	
	
	public static int nPacket=0;
	public static int nPacket2=0;
	
	public static double packetRate=0;
	public static double packetRate2=0;
	public static double packetRate3=0;
	public static double packetRate4=0;
	
	long startTime;
	long currTime;
	long rateLastTime;
	long lastTime;
	
	public static String getAllTokenBucketsString(){
		String str = "";
		Iterator<Integer> iterator = Main.nodes.keySet().iterator();
		while(iterator.hasNext()){
			str += Main.nodes.get(iterator.next()).tokenBucketString() + " ";
		}
		return str;
	}

	public static String getAllShdwPacketStats(){
		
		ArrayList<Integer> neighborList = new ArrayList<Integer>();
		for(Integer neighbor : Main.neighbors.keySet()){
			neighborList.add(neighbor);
		}
		Collections.sort(neighborList);
		
		String ret = "ShadowPackets\nDst\tGen\tDrp\t";
		for(int i=0;i<neighborList.size(); i++){
			ret += "+" + neighborList.get(i) + "\t";
		}
		for(int i=0;i<neighborList.size(); i++){
			ret += "-" + neighborList.get(i) + "\t";
		}
		ret += "\n";
		Iterator<Integer> it = Main.nodes.keySet().iterator();
		while(it.hasNext()){
			int dest = it.next();
			ret += dest + "\t" + Main.shdwGen.get(dest) + "\t" + Main.shdwDrop.get(dest) + "\t";
			for(int i=0;i<neighborList.size(); i++){
				ret += Main.shdwRecv.get(dest).get(neighborList.get(i)) + "\t";
			}
			for(int i=0;i<neighborList.size(); i++){
				ret += Main.shdwSent.get(dest).get(neighborList.get(i)) + "\t";
			}
			 ret += "\n";
		}
		return ret;
	}
	
public static String getAllTokenBucketStats(){
		
		ArrayList<Integer> neighborList = new ArrayList<Integer>();
		for(Integer neighbor : Main.neighbors.keySet()){
			neighborList.add(neighbor);
		}
		Collections.sort(neighborList);
		
		String ret = "Token Buckets\nDst\t";
		for(int i=0;i<neighborList.size(); i++){
			ret += "+" + neighborList.get(i) + "\t";
		}
		for(int i=0;i<neighborList.size(); i++){
			ret += "-" + neighborList.get(i) + "\t";
		}
		ret += "\n";
		Iterator<Integer> it = Main.nodes.keySet().iterator();
		while(it.hasNext()){
			int dest = it.next();
			ret += dest + "\t";
			for(int i=0;i<neighborList.size(); i++){
				ret += Main.toknRecv.get(dest).get(neighborList.get(i)) + "\t";
			}
			for(int i=0;i<neighborList.size(); i++){
				ret += Main.toknSent.get(dest).get(neighborList.get(i)) + "\t";
			}
			 ret += "\n";
		}
		return ret;
	}
	
	public void run(){
		startTime = System.currentTimeMillis();
		currTime = System.currentTimeMillis();
		rateLastTime = 0;
		lastTime=0;
		
		try {
			while(!Configurations.SYSTEM_HALT){
				
				
				
				long time = System.currentTimeMillis() - Main.startTime;
				
				//Printing Block : executes after every 1 second
				if(time - lastTime > 1000){
					
					if(Main.DEBUG){
						String str="";					
						synchronized(Main.lastLock){
							str += "INFO: " + time + " / " + Main.duration + "\n";
							str += "[" + Main.iteration + "]DataPacketsGenerated: " + Main.lastDataPacketsGenerated + ", DataPacketsReceived: " + Main.lastDataPacketsReceived + ", DataPacketsSent: " + Main.lastDataPacketsSent + ", DataPacketsConsumed: " + Main.lastDataPacketsConsumed + "\n";
							str += "[" + Main.iteration + "]ShdwPacketsGenerated: " + Main.lastShadowPacketsGenerated + ", ShdwPacketsReceived: " + Main.lastShadowPacketsReceived + ", ShdwPacketsSent: " + Main.lastShadowPacketsSent + "\n";
							str += "[" + Main.iteration + "] RealQueue: " + Main.getRealQueues() + " inputBuffer: " + Main.inputBufferSize + "\n";
							str += "[" + Main.iteration + "]TokenBuckets: " + Router.getAllTokenBucketsString() + "\n";
							str += "[" + Main.iteration + "]ShadowQueues:" + Configurations.hashToString(Main.getShadowQueues()) + "\n";
							str += "[" + Main.iteration + "]packetRate: " + packetRate + " packetRate2: " + packetRate2 + " packetRate3: " + packetRate3 + " packetRate4 " + packetRate4;
							str += "\n";
							str += getAllShdwPacketStats() + "\n";
							str += getAllTokenBucketStats() + "\n";
							System.out.println(str + "\n\n\n");
							Main.lastDataPacketsConsumed = Main.lastDataPacketsGenerated = Main.lastDataPacketsReceived = Main.lastDataPacketsSent = 0;
							Main.lastShadowPacketsGenerated = Main.lastShadowPacketsReceived = Main.lastShadowPacketsSent = 0;
							Main.resetStats();
							lastTime = time;
						}
					}
					
					
				}
				
				
				
				//Data Packet rate calculation blocks : executes after every 1 second
				nPacket++;
				nPacket2++;
				currTime = System.currentTimeMillis();
				if(currTime - rateLastTime > 1000){
					packetRate = nPacket*1000.0 / (currTime - rateLastTime);  
					nPacket=0;
					
					packetRate2 = nPacket2*1000.0 / (currTime - startTime);
					packetRate3 = Main.dataPacketsSent * 1000.0 / (currTime - startTime);
					
					synchronized(Main.lastDataPacketsSentLock){
						packetRate4 = Main.lastDataPacketsSent2 * 1000.0 / (currTime - rateLastTime) ;
						Main.lastDataPacketsSent2=0;
					}
					rateLastTime = currTime;
					if(Main.verbose){
						System.out.println("DATA: " + this + " packetRate: " + packetRate + " packetRate2: " + packetRate2 + " packetRate3: " + packetRate3 + " packetRate4 " + packetRate4 );
					}
					
				}
				
				
				//
				if (System.currentTimeMillis() - Main.startTime > Main.duration || Configurations.FATAL_ERROR){
					Configurations.SYSTEM_HALT = true;
					//System.out.println("INFO: Stopping the system");
					CommandPromt.printStats();
					System.exit(0);
					continue;
				}

				/*if(Configurations.DEBUG_ON){
				try{
					sleep(Configurations.SLOW_DOWN_FACTOR);
				}catch(InterruptedException e){
					System.out.println("DATA: Router: Got interrupted");
				}
			}*/

				//DataPacket packet = Main.inputBuffer.poll();

				//Dealing with Packet batches

				
				DataPacket peekPacket = Main.inputBuffer.peek();
				DataPacket packet = null;

				if(peekPacket==null) continue;

				if (peekPacket.batch == true){
					Iterator<Integer> iterator = Main.neighbors.keySet().iterator();
					//boolean shouldSleep = false;
					long sum=0;
					while(iterator.hasNext()){
							sum += Main.neighbors.get(iterator.next()).realQueue.size();	
					}
					
					if(sum > 10000){
						sleep(1);
					}
					//System.out.println("DATA: Router batch packet " + batchCount + " startSequence " + peekPacket.startSequenceNumber + " end seq " + peekPacket.endSequenceNumber);
					packet = new DataPacket(peekPacket.flowID, peekPacket.source, peekPacket.destination, peekPacket.startSequenceNumber + batchCount);
					batchCount++;
					if (peekPacket.startSequenceNumber + batchCount > peekPacket.endSequenceNumber){
						batchCount = 0;
						DataPacket discardBatchPacket = Main.inputBuffer.poll();
						//System.out.println("DATA: Batch packet discarded at count " + batchCount );
					}		
				}else if(peekPacket.batch == false){
					packet = Main.inputBuffer.poll();
				}
				
				
				//TokenBucket algorithm
				packet.path.add((char) Main.ID);	
				Main.dataPacketsRouted++;
				
				synchronized(Main.inputBufferSizeLock){
					Main.inputBufferSize--;
				}
				
				if(packet.destination == Main.ID){
					consumePacket(packet);
				} else if(Main.nodes.containsKey(packet.destination)){
					Node destination = Main.nodes.get(packet.destination);
					int link=0;
					
						link = destination.getTokenBucket();
					
					Neighbor neighbor = Main.neighbors.get(link);

					try {
					
						neighbor.realQueue.put(packet);
						//System.out.println("DATA: Router: routing " + packet + " to " + neighbor);
					} catch (InterruptedException e) {
						System.out.println("DATA: Router: Error transferring " + packet);
						e.printStackTrace();
					}
				}else{
					System.out.println("DATA: Router: Unknown destination. Dropping " + packet);
				}




			}
		
			
		} catch(Throwable e){
			e.printStackTrace();
			System.out.println(this + " FATAL ERROR " + e.getMessage());
			System.exit(1);
		}
	}

	public void consumePacket(DataPacket packet){
		//TODO: Assuming at most one flow source
		//System.out.println("DATA: Consuming " + packet);
		if(Main.flowStatReceived.containsKey(packet.source)){
			Main.flowStatReceived.get(packet.source).addPacket(packet);
		}else{
			Main.flowStatReceived.put(packet.source, new FlowStat(packet));
		}
		synchronized(Main.lastLock){
			Main.lastDataPacketsConsumed++;
		}
	}

	public String toString(){
		return "Router";
	}

}
