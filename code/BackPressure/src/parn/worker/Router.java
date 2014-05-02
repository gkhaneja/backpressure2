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
	
	public String getAllTokenBucketsString(){
		String str = "";
		Iterator<Integer> iterator = Main.nodes.keySet().iterator();
		while(iterator.hasNext()){
			str += Main.nodes.get(iterator.next()).tokenBucketString() + "\t";
		}
		return str;
	}

	public void run(){
		try {
			while(!Configurations.SYSTEM_HALT){
				

				long time = System.currentTimeMillis() - Main.startTime;
				if(time%1000 == 0 && time!=lastTime){
					System.out.println("INFO: " + time + " / " + Main.duration);
					System.out.println("DATA: TokenBuckets: \t" + getAllTokenBucketsString());
					lastTime = time;
				}
				if (System.currentTimeMillis() - Main.startTime > Main.duration){
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
					int link = destination.getTokenBucket();

					Neighbor neighbor = Main.neighbors.get(link);

					try {
						Main.dataPacketsSent++;
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
		}catch(Exception e){
			System.out.println("DATA: FATAL: Router thread has gotten the follwoing error: ");
			e.printStackTrace();
			System.out.println("KILL: Killing the program");
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
