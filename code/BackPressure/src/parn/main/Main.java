package parn.main;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import parn.node.Neighbor;
import parn.node.Node;
import parn.packet.ControlPacket;
import parn.packet.DataPacket;
import parn.packet.ShadowQueue;
import parn.worker.CommandPromt;
import parn.worker.Flow;
import parn.worker.Router;
import parn.worker.ShadowPacketGenerator;

public class Main {

	
	//Entities
	public static HashMap<Integer, Node> nodes;
	public static HashMap<Integer, Neighbor> neighbors;
	public static LinkedBlockingQueue<DataPacket> inputBuffer;
	public static HashMap<Integer, ShadowQueue> shadowQueues;
	
	
	//Global variables
	public static int ID;
	public static int nextFlowID;
	public static int M;
	public static double epsilon;
	public static int Capacity;
	public static double stabilityDiff = 0.02;
	public static long bandwidth = 1000000000 / 8;
	public static int usePropSplitting = 0;
	public static int initializeShadowQueues = 0;
	public static long duration = 0;
	public static long startTime = System.currentTimeMillis();
	public static boolean error = false;
	public static int iteration=0;
	//phase: 0-sending/receiving to shadow packets (shadowPacketGenerator is active) , 1-sending/receiving to shadow queues (controlPacketSenders are active)
	public static int iterationPhase=1;

	
	
	//Threads
	public static Router router;
	public static ShadowPacketGenerator shadowPacketGenerator;
	public static HashMap<Integer, Flow> flows;
	public static CommandPromt commandPromt = new CommandPromt();
	

	//Locks and Notifiers
	private static Object shadowQueueLock;
	public static Object receivedShadowQueueLock = new Object();
	public static Object receivedShadowPacketLock = new Object();
	public static Object iterationPhaseLock = new Object();
	public static Object sentShadowQueueLock = new Object();
	private static Object controlReceiverStatsLock = new Object();
	private static Object controlSenderStatsLock = new Object();
	
	
	//Shared and synchronized (lock protected) fields:
	public static int nShadowQueueReceived;
	public static int nShadowQueuesSent=0;
	public static int nShadowPacketReceived;
	
	//Measurements per flow
	public static HashMap<Integer, FlowStat> flowStatReceived = new HashMap<Integer, FlowStat>();
	
	
	
	//Measurements for shadow packets
	//Can use the shadowQueueLock itself
	public static int shadowPacketsGenerated=0;
	public static int extraShadowPacketsGenerated=0;
	public static int shadowPacketsReceived=0;
	//No locking required for this one - since a single thread - ShadowQueueGenerator will generate shadow packets 
	public static int shadowPacketsSent=0;
	
	
	
	//Measurements for data packets
	public static int dataPacketsGenerated=0;
	public static Object dataPacketStatLock = new Object();
	public static int dataPacketsSent=0;
	public static int dataPacketsReceived=0;
	public static int dataPacketSize=930;
	//Not being used 
	public static int averageDataPacketSize=0;
	
	//Measurements for control packets
	public static int controlPacketsSent=0;
	public static int controlPacketsReceived=0;
	public static int controlBytesSent=0;
	public static int controlBytesReceived=0;

	public static boolean init(String confFile){
		//TODO: change static id assignment: Done
		//ID=id;
		nodes = new HashMap<Integer, Node>();
		neighbors = new HashMap<Integer, Neighbor>();
		inputBuffer = new LinkedBlockingQueue<DataPacket>();
		shadowQueues = new HashMap<Integer, ShadowQueue>();
		flows = new HashMap<Integer, Flow>();
		router = new Router();
		shadowPacketGenerator = new ShadowPacketGenerator();
		nextFlowID=0;
		nShadowQueueReceived=0;
		M=0;
		epsilon=0.1;
		//TODO: change this. For testing.
		Capacity = 1;	
		shadowQueueLock= new Object();
		receivedShadowQueueLock = new Object();
		receivedShadowPacketLock = new Object();
		
		if(!parseConfFile2(confFile)) return false;
		
		//Initializations
		initNeighbors();			
		Iterator<Integer> iterator = nodes.keySet().iterator();
		while(iterator.hasNext()){
			nodes.get(iterator.next()).init();
		}
		printNodes();
		printNeighbors();
		System.out.println("Given id: " + Main.ID);
		System.out.println("No. of nodes: " + nodes.size());
		System.out.println("No. of neighbors: " + neighbors.size());

		startConnectionWorkers();
		generateFlows();
		
		shadowPacketGenerator.start();
		router.start();
		commandPromt.start();

		

		return true;
	}
	
	public static InetAddress getAddress(String ip) throws UnknownHostException{
		String[] addrStr = ip.split("\\.");
		//System.out.println(addrStr.length + " " + parts[1]);
		byte[] addrByte = new byte[4];
		for (int i=0;i<4;i++){
			addrByte[i] = Byte.parseByte(addrStr[i]);
		}									
		InetAddress address = InetAddress.getByAddress(addrByte);
		return address;
	}
	
	public static boolean parseConfFile2(String confFile){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(new File(confFile)));
			String line;
			String[] parts;
 			//first line contains M, epsilon, prop, pathlength, time
			line = reader.readLine();
			parts = line.split("\t");
			Main.M = Integer.parseInt(parts[0]);
			Main.epsilon = Double.parseDouble(parts[1]);
			Main.usePropSplitting = Integer.parseInt(parts[2]);
			Main.initializeShadowQueues = Integer.parseInt(parts[3]);
			Main.duration = Long.parseLong(parts[4]);
			
			line = reader.readLine();
			parts = line.split("\t");
			Main.ID = Integer.parseInt(parts[1]);
			
			line = reader.readLine();
			parts = line.split("\t");			
			nodes.put(Main.ID, new Node(Main.ID, Main.getAddress(parts[1])));
			shadowQueues.put(Main.ID, new ShadowQueue(Main.ID, 0));
			
			line = reader.readLine();
			parts = line.split("\t");			
			int nNeighbors = Integer.parseInt(parts[1]);			
			for(int i=0; i<nNeighbors; i++){
				line = reader.readLine();
				parts = line.split("\t");
				neighbors.put(Integer.parseInt(parts[0]), new Neighbor(Integer.parseInt(parts[2])));
			}
			
			line = reader.readLine();
			parts = line.split("\t");			
			int nNodes = Integer.parseInt(parts[1]);			
			for(int i=0; i<nNodes; i++){
				line = reader.readLine();
				parts = line.split("\t");			
				int nodeId = Integer.parseInt(parts[0]);
				nodes.put(nodeId, new Node(Integer.parseInt(parts[0]), Main.getAddress(parts[1]), Integer.parseInt(parts[2])));
				shadowQueues.put(nodeId, new ShadowQueue(nodeId, 0));
			}
			
			line = reader.readLine();
			parts = line.split("\t");			
			int nFlows = Integer.parseInt(parts[1]);			
			for(int i=0; i<nFlows; i++){
				line = reader.readLine();
				parts = line.split("\t");				
				flows.put(Integer.parseInt(parts[0]), new Flow(Integer.parseInt(parts[0]), Main.ID, Integer.parseInt(parts[1]), Double.parseDouble(parts[2])));
			}
			
			
			reader.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
			return false;
		}catch(Exception e){
			System.out.println("File not formatted correctly");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean parseConfFile(String confFile){
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
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			System.out.println("Conf file is not formatted correctly");
			e.printStackTrace();
			return false;
		}
	}
	
	
	public static void notifyShadowQueueArrival(){
		synchronized(receivedShadowQueueLock){
			nShadowQueueReceived++;
			System.out.println("CONTROL: Received " + nShadowQueueReceived + " shadow-queues for iteration " + Main.iteration);
			if(nShadowQueueReceived == Main.neighbors.size()){
				
				receivedShadowQueueLock.notifyAll();
			}
		}
		
	}
	
	public static void notifyShadowPacketArrival(){
		synchronized(receivedShadowPacketLock){
			nShadowPacketReceived++;
			System.out.println("CONTROL: Received " + nShadowPacketReceived + " shadow-packets for iteration " + Main.iteration);
			if(nShadowPacketReceived == Main.neighbors.size()){			
				receivedShadowPacketLock.notifyAll();
			}
		}
	}
	
	public static void updateControlReceiverStats(ControlPacket packet){
		synchronized(Main.controlReceiverStatsLock){
			try {
				Main.controlBytesReceived += Main.sizeof(packet);
				Main.controlPacketsReceived++;
			} catch (IOException e) {
				System.out.println("ERROR: couldn't coount " + packet);
			}
		}
	}
	
	public static void updateControlSenderStats(ControlPacket packet){
		synchronized(Main.controlSenderStatsLock){
			try {
				Main.controlBytesSent += Main.sizeof(packet);
				Main.controlPacketsSent++;
			} catch (IOException e) {
				System.out.println("ERROR: couldn't coount " + packet);
			}
		}
	}
	
	
	
	public static HashMap<Integer, ShadowQueue> getShadowQueues(){
		//TODO: Do we need locking here - synchronized with Main.updateShadowQueue(dest, change) ?
		//TODO: Should we send the reference to original or should we return a copy (expensive) ?
		return Main.shadowQueues;
	}
	
	public static void updateShadowQueue(ControlPacket packet){
		if(packet.type!=Configurations.SHADOW_PACKET_TYPE){
			System.out.println("ERROR: updateShadowQueue: wrong packet " + packet);
			return;
		}
		
		if(packet.shadowPackets.size() > 1){
			System.out.println("CONTROL: INFO: Proportional splitting is ON");
		}
		
		synchronized(shadowQueueLock){
			Iterator<Integer> iterator = packet.shadowPackets.keySet().iterator();			
			while(iterator.hasNext()){
				int destination = iterator.next();
				int nPackets = packet.shadowPackets.get(destination);
				Main.shadowPacketsReceived += nPackets;
				//System.out.println("DEBUG: " + Main.nShadowQueueReceived);
				if(destination==Main.ID){
					continue;
				}
				if(shadowQueues.containsKey(destination)){
					shadowQueues.get(destination).update(nPackets);
				}	
			}
			Main.notifyShadowPacketArrival();
		}
	}
	
	//Just used by flow generators to add shadow packets to shadow queues
	public static void addShadowPackets(int destination, int nShadowPackets){
		
		
		if(destination==Main.ID){
			return;
		}
		synchronized(shadowQueueLock){
			if(shadowQueues.containsKey(destination)){
				shadowQueues.get(destination).update(nShadowPackets);
				if(nShadowPackets==2){
					Main.extraShadowPacketsGenerated++;
				}
				Main.shadowPacketsGenerated += nShadowPackets;
				
			}
		}
		
	}
	
	public static void startConnectionWorkers(){
		Iterator<Integer> neighborIterator = neighbors.keySet().iterator();
		while(neighborIterator.hasNext()){
			int neighborId = neighborIterator.next();
			Neighbor neighbor = neighbors.get(neighborId);

			
			neighbor.start();
		}
	}
	
	public static void initNeighbors(){
		Iterator<Integer> neighborIterator = neighbors.keySet().iterator();
		while(neighborIterator.hasNext()){
			int neighborId = neighborIterator.next();
			Neighbor neighbor = neighbors.get(neighborId);
			neighbor.node = nodes.get(neighborId);
			
		}
	}

	public static void generateFlows(){
		//TODO: For testing, generating preset flow-pattern - One flow for each node. This should be replaced appropriately. 
		if(Configurations.TURN_OFF_FLOWS){
			return;
		}
		Random rand = new Random();
		Iterator<Integer> iterator = flows.keySet().iterator();
		while(iterator.hasNext()){
			int flowId = iterator.next();
			Main.flows.get(flowId).start();			
		}
	}





	public static int sizeof(Object obj) throws IOException {

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
        objectOutputStream.close();

        return byteOutputStream.toByteArray().length;
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
		System.out.println("Given file: " + args[0]);
		//System.out.println()
		Main.init(args[0]);
	}

}
