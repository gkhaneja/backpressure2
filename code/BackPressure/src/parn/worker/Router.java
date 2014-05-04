package parn.worker;

import java.util.Iterator;

import parn.main.Configurations;
import parn.main.FlowStat;
import parn.main.Main;
import parn.node.Neighbor;
import parn.node.Node;
import parn.packet.DataPacket;

public class Router extends Thread {

	long lastTime=0;
	
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
	
	public static String getAllTokenBucketsString(){
		String str = "";
		Iterator<Integer> iterator = Main.nodes.keySet().iterator();
		while(iterator.hasNext()){
			str += Main.nodes.get(iterator.next()).tokenBucketString() + " ";
		}
		return str;
	}

	public void run(){
		startTime = System.currentTimeMillis();
		currTime = System.currentTimeMillis();
		rateLastTime = System.currentTimeMillis();
		
		try {
			while(!Configurations.SYSTEM_HALT){
				
				
				
				long time = System.currentTimeMillis() - Main.startTime;
				if(time - lastTime > 1000){
					System.out.println("INFO: " + time + " / " + Main.duration);
					//System.out.println("DATA: TokenBuckets: \t" + getAllTokenBucketsString());
					System.out.println("RealQueue: " + Main.getRealQueues() + " inputBuffer: " + Main.inputBuffer.size());
					System.out.println("TokenBuckets: " + Router.getAllTokenBucketsString());
					lastTime = time;
					
				}
				
				
				nPacket++;
				nPacket2++;
				currTime = System.currentTimeMillis();
				if(currTime - rateLastTime > 1000){
					packetRate = nPacket*1000.0 / (currTime - rateLastTime);  
					nPacket=0;
					
					packetRate2 = nPacket2*1000.0 / (currTime - startTime);
					packetRate3 = Main.dataPacketsSent * 1000.0 / (currTime - startTime);
					
					synchronized(Main.lastSecondDataPacketsSentLock){
						packetRate4 = Main.lastSecondDataPacketsSent * 1000.0 / (currTime - rateLastTime) ;
						Main.lastSecondDataPacketsSent=0;
					}
					rateLastTime = currTime;
					System.out.println("DATA: " + this + " packetRate: " + packetRate + " packetRate2: " + packetRate2 + " packetRate3: " + packetRate3 + " currTime: " + currTime / 1000);
					
					
					
					
					
					//packetRate = packetRate*4.0/5.0 + 1000.0*nPacket/((time - lastTime)*5.0);
				}
				
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
				Main.dataPacketsReceived++;
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


//				packetRate = packetRate*4.0/5.0 + 1000.0/((oneMoreTime - System.currentTimeMillis())*5.0);
//				oneMoreTime = System.currentTimeMillis();
//				System.out.println("DATA: " + this + " packetRate: " + packetRate + " oneMoreTime: " + oneMoreTime);

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
	}

	public String toString(){
		return "Router";
	}

}
