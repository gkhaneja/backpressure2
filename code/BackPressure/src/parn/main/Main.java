package parn.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import parn.node.Neighbor;
import parn.node.Node;
import parn.packet.DataPacket;
import parn.packet.ShadowQueue;
import parn.worker.Flow;
import parn.worker.Router;

public class Main {

	//TODO: where are back pressure weighs calculated ? I have pnd's and pnj's. Where to calculate wnj[d] = pnd - pnj ?
	//TODO: 
	
	public static int ID;
	public static HashMap<Integer, Node> nodes;
	public static HashMap<Integer, Neighbor> neighbors;
	public static LinkedBlockingQueue<DataPacket> inputBuffer;
	private static HashMap<Integer, ShadowQueue> shadowQueues;
	public static HashMap<Integer, Flow> flows;
	public static Router router;
	public static int nextFlowID;

	public static boolean init(int id, String confFile){
		//TODO: change static id assignment: Done
		ID=id;
		nodes = new HashMap<Integer, Node>();
		neighbors = new HashMap<Integer, Neighbor>();
		inputBuffer = new LinkedBlockingQueue<DataPacket>();
		shadowQueues = new HashMap<Integer, ShadowQueue>();
		flows = new HashMap<Integer, Flow>();
		router = new Router();
		nextFlowID=0;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(confFile)));
			String line;
			while((line=reader.readLine())!=null){
				String[] parts = line.split("\t");
				int nodeId = Integer.parseInt(parts[0]);
				String[] addrStr = parts[1].split("\\.");
				//System.out.println(addrStr.length + " " + parts[1]);
				byte[] addrByte = new byte[4];
				for (int i=0;i<4;i++){
					addrByte[i] = Byte.parseByte(addrStr[i]);
				}									
				InetAddress address = InetAddress.getByAddress(addrByte);
				nodes.put(nodeId, new Node(nodeId, address));
				shadowQueues.put(nodeId, new ShadowQueue(nodeId, 0));

				if(nodeId==ID){
					for(int i=2; i<parts.length; i++){
						String[] neighbor = parts[i].split(",");
						int neighborId = Integer.parseInt(neighbor[0]);
						int neighborPort = Integer.parseInt(neighbor[1]);
						neighbors.put(neighborId, new Neighbor(neighborPort));
					}
				}
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			System.out.println("Conf file is not formatted correctly");
			e.printStackTrace();
			return false;
		}


		startConnectionWorkers();
		generateFlows();
		

		router.start();

		//printNodes();
		//printNeighbors();

		return true;
	}
	
	public static HashMap<Integer, ShadowQueue> getShadowQueues(){
		//TODO: Do we need locking here - synchronized with Main.updateShadowQueue(dest, change) ?
		//TODO: Should we send the reference to original or should we return a copy (expensive) ?
		return Main.shadowQueues;
	}
	
	public static void updateShadowQueue(int destination, int change){
		Object lock = new Object();
		synchronized(lock){
			if(shadowQueues.containsKey(destination)){
				shadowQueues.get(destination).update(change);//, change + shadowQueues.get(desti))
			}
		}
	}
	
	public static void startConnectionWorkers(){
		Iterator<Integer> neighborIterator = neighbors.keySet().iterator();
		while(neighborIterator.hasNext()){
			int neighborId = neighborIterator.next();
			Neighbor neighbor = neighbors.get(neighborId);
			neighbor.node = nodes.get(neighborId);

			//TODO: start the thread
			neighbor.start();
		}
	}

	public static void generateFlows(){
		//TODO: For testing, generating preset flow-pattern - One flow for each node. This should be replaced appropriately. 
		Random rand = new Random();
		Iterator<Integer> nodeIterator = nodes.keySet().iterator();
		while(nodeIterator.hasNext()){
			int nodeId = nodeIterator.next();
			if(Main.ID!=nodeId){
				int flowId = Main.getNextFlowID();
				Flow flow = new Flow(flowId, Main.ID, nodeId, 0.2);
				flows.put(flowId, flow);
				flow.start();
			}
		}
	}



	public static int getNextFlowID(){
		Object lock = new Object();
		int newFlowId=0;
		synchronized(lock){
			newFlowId = Main.nextFlowID;
			Main.nextFlowID++;
		}
		return newFlowId;
	}

	static void printNeighbors(){
		System.out.println("Neighbors:");
		Iterator<Integer> neighborIterator = neighbors.keySet().iterator();
		while(neighborIterator.hasNext()){
			int neighborId = neighborIterator.next();
			System.out.println(neighbors.get(neighborId));
		}
	}

	static void  printNodes(){
		System.out.println("Nodes:");
		Iterator<Integer> nodeIterator = nodes.keySet().iterator();
		while(nodeIterator.hasNext()){
			int nodeId = nodeIterator.next();
			System.out.println(nodes.get(nodeId));						
		}
	}

	//public static void print(String){

	//}


	public static void main(String[] args) {
		//Main.init(1,"bp.conf");
		Main.init(2,"bp.conf");

	}

}
