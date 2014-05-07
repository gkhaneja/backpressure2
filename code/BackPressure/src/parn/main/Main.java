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
import parn.worker.Terminator;

public class Main {


	//Entities
	public static HashMap<Integer, Node> nodes;
	public static HashMap<Integer, Neighbor> neighbors;
	public static LinkedBlockingQueue<DataPacket> inputBuffer;
	public static HashMap<Integer, ShadowQueue> shadowQueues;


	//Global variables
	public static int ID;
	public static double M = 1;
	public static double epsilon = 0;
	public static int Capacity;
	public static double stabilityDiff = 0.05;
	public static double itThreshold = 20;
	public static long bandwidth = 1000000000 / (8);
	public static int usePropSplitting = 1;
	public static int useNeighborOp = 1;
	public static int initializeShadowQueues = 1;
	public static long duration = Configurations.DURATION;
	public static long startTime = System.currentTimeMillis();
	public static boolean error = false;
	public static int iteration=0;
	public static int TOP_K=2;
	public static double shadowPaketRateFactor=1.0;
	//phase: 0-sending/receiving to shadow packets (shadowPacketGenerator is active) , 1-sending/receiving to shadow queues (controlPacketSenders are active)
	public static int iterationPhase=1;
	public static int stableCounter=0;



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
	public static Object extraShadowPacketGeneratedLock = new Object();
	public static Object dataPacketsDroppedLock = new Object();
	public static Object dataPacketsSentLock = new Object();


	//Debug
	public static boolean verbose = false;
	public static boolean DEBUG = true;	
	public static Object lastLock = new Object();
	
	public static int lastDataPacketsSent=0;
	public static int lastDataPacketsSent2=0;
	public static Object lastDataPacketsSentLock = new Object();
	public static HashMap<Integer, Integer> lastDataSent = new HashMap<Integer, Integer>();
	
	public static int lastDataPacketsReceived=0;
	public static Object lastDataPacketsReceivedLock=0;
	public static HashMap<Integer, Integer> lastDataRecv = new HashMap<Integer, Integer>();
	
	public static int lastShadowPacketsSent=0;
	public static Object lastShadowPacketsSentLock = new Object();
	public static HashMap<Integer, Integer> lastShdwSent = new HashMap<Integer, Integer>();
	
	public static int lastShadowPacketsReceived=0;
	public static Object lastShadowPacketsReceivedLock = new Object();
	public static HashMap<Integer, Integer> lastShdwRecv = new HashMap<Integer, Integer>();
	
	public static int lastDataPacketsGenerated = 0;
	public static Object lastDataPacketsGeneratedLock = new Object();
	public static HashMap<Integer, Integer> lastDataGen = new HashMap<Integer, Integer>();
	
	public static int lastShadowPacketsGenerated = 0;
	public static Object lastShadowPacketsGeneratedLock = new Object();
	public static HashMap<Integer, Integer> lastShdwGen = new HashMap<Integer, Integer>();
	
	public static int lastDataPacketsConsumed = 0;
	public static Object lastDataPacketsConsumedLock = new Object();
	public static HashMap<Integer, Integer> lastDataConsumed = new HashMap<Integer, Integer>();
	
	public static int inputBufferSize = 0;
	public static Object inputBufferSizeLock = new Object();
	
	public static HashMap<Integer, HashMap<Integer, Integer>> shdwSent =  new HashMap<Integer, HashMap<Integer, Integer>>();
	public static HashMap<Integer, HashMap<Integer, Integer>> shdwRecv =  new HashMap<Integer, HashMap<Integer, Integer>>();
	public static HashMap<Integer, HashMap<Integer, Integer>> toknSent =  new HashMap<Integer, HashMap<Integer, Integer>>();
	public static HashMap<Integer, HashMap<Integer, Integer>> toknRecv =  new HashMap<Integer, HashMap<Integer, Integer>>();
	public static HashMap<Integer, Integer> shdwDrop = new HashMap<Integer, Integer>();
	public static Object shdwDropLock = new Object();
	public static HashMap<Integer, Integer> shdwGen = new HashMap<Integer, Integer>();
	public static Object shdwGenLock = new Object();
	
	//Shared and synchronized (lock protected) fields:
	public static int nShadowQueueReceived = 0;
	public static int nShadowQueuesSent=0;
	public static int nShadowPacketReceived = 0;

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
	public static Object dataPacketsReceivedLock = new Object();
	public static int dataPacketsRouted=0;
	public static int dataPacketSize=Configurations.PAYLOAD_SIZE + 450;
	public static int dataPacketsDropped=0;
	//Not being used 
	public static int averageDataPacketSize=0;


	//Measurements for control packets
	public static int controlPacketsSent=0;
	public static int controlPacketsReceived=0;
	public static int controlBytesSent=0;
	public static int controlBytesReceived=0;

	public static boolean init(String confFile){
		try{
			//TODO: change static id assignment: Done
			//ID=id;
			nodes = new HashMap<Integer, Node>();
			neighbors = new HashMap<Integer, Neighbor>();
			inputBuffer = new LinkedBlockingQueue<DataPacket>();
			shadowQueues = new HashMap<Integer, ShadowQueue>();
			flows = new HashMap<Integer, Flow>();
			router = new Router();
			shadowPacketGenerator = new ShadowPacketGenerator();	
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

			resetStats();
			
			startConnectionWorkers();
			generateFlows();

			shadowPacketGenerator.start();
			router.start();
			commandPromt.start();
			
			//Terminator terminator = new Terminator();
			//terminator.start();

		}catch(Throwable e){
			e.printStackTrace();
			System.out.println("MAIN: FATAL ERROR");
			Configurations.FATAL_ERROR = true;
		}



		return true;
	}

	public static void resetStats(){
		Main.toknRecv = new HashMap<Integer, HashMap<Integer,Integer>>();
		Iterator<Integer> it = Main.nodes.keySet().iterator();
		while(it.hasNext()){
			int dest = it.next();
			Iterator<Integer> it2 = Main.neighbors.keySet().iterator();
			HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
			while(it2.hasNext()){
				temp.put(it2.next(), 0);
			}
			Main.toknRecv.put(dest, temp);
		}
		
		Main.toknSent = new HashMap<Integer, HashMap<Integer,Integer>>();
		it = Main.nodes.keySet().iterator();
		while(it.hasNext()){
			int dest = it.next();
			Iterator<Integer> it2 = Main.neighbors.keySet().iterator();
			HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
			while(it2.hasNext()){
				temp.put(it2.next(), 0);
			}
			Main.toknSent.put(dest, temp);
		}
		
		Main.shdwRecv = new HashMap<Integer, HashMap<Integer,Integer>>();
		it = Main.nodes.keySet().iterator();
		while(it.hasNext()){
			int dest = it.next();
			Iterator<Integer> it2 = Main.neighbors.keySet().iterator();
			HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
			while(it2.hasNext()){
				temp.put(it2.next(), 0);
			}
			Main.shdwRecv.put(dest, temp);
		}
		
		Main.shdwSent = new HashMap<Integer, HashMap<Integer,Integer>>();
		it = Main.nodes.keySet().iterator();
		while(it.hasNext()){
			int dest = it.next();
			Iterator<Integer> it2 = Main.neighbors.keySet().iterator();
			HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
			while(it2.hasNext()){
				temp.put(it2.next(), 0);
			}
			Main.shdwSent.put(dest, temp);
		}
		
		shdwGen = new HashMap<Integer, Integer>();
		it = Main.nodes.keySet().iterator();
		while(it.hasNext()){
			int dest = it.next();
			Main.shdwGen.put(dest, 0);
		}
		
		shdwDrop = new HashMap<Integer, Integer>();
		it = Main.nodes.keySet().iterator();
		while(it.hasNext()){
			int dest = it.next();
			Main.shdwDrop.put(dest, 0);
		}
	}
	
	public static InetAddress getAddress(String ip) throws UnknownHostException{
		String[] addrStr = ip.split("\\.");
		//System.out.println(addrStr.length + " " + parts[1]);
		byte[] addrByte = new byte[4];
		for (int i=0;i<4;i++){
			addrByte[i] = (byte) Integer.parseInt(addrStr[i]);
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
			Main.TOP_K = Integer.parseInt(parts[2]);
			Main.useNeighborOp = Integer.parseInt(parts[3]);
			Main.initializeShadowQueues = Integer.parseInt(parts[3]);
			//Main.duration = Long.parseLong(parts[4]);
			//Configurations.CONTROL_INTERVAL = Long.parseLong(parts[5]);
			System.out.println("Params Formatted Correctly");

			line = reader.readLine();
			parts = line.split("\t");
			Main.ID = Integer.parseInt(parts[1]);
			
			
			line = reader.readLine();
			parts = line.split("\t");			
			nodes.put(Main.ID, new Node(Main.ID, Main.getAddress(parts[1])));
			shadowQueues.put(Main.ID, new ShadowQueue(Main.ID, 0));
			System.out.println("ID Formatted Correctly");
			
			line = reader.readLine();
			parts = line.split("\t");			
			int nNeighbors = Integer.parseInt(parts[1]);			
			for(int i=0; i<nNeighbors; i++){
				line = reader.readLine();
				parts = line.split("\t");
				neighbors.put(Integer.parseInt(parts[0]), new Neighbor(Integer.parseInt(parts[2])));
			}
			System.out.println("Neighbors Formatted Correctly");
			
			line = reader.readLine();
			parts = line.split("\t");			
			int nNodes = Integer.parseInt(parts[1]);			
			for(int i=0; i<nNodes; i++){
				line = reader.readLine();
				parts = line.split("\t");			
				int nodeId = Integer.parseInt(parts[0]);
				nodes.put(nodeId, new Node(Integer.parseInt(parts[0]), Main.getAddress(parts[1]), Configurations.PACKET_RATE*Integer.parseInt(parts[2])));
				if(initializeShadowQueues==1){
					System.out.println("CONTROL: Initializing shadow queue " + nodeId + " with " + Configurations.PACKET_RATE*Integer.parseInt(parts[2]));
					shadowQueues.put(nodeId, new ShadowQueue(nodeId, Integer.parseInt(parts[2])));
				}else{
					shadowQueues.put(nodeId, new ShadowQueue(nodeId, 0));
				}
			}
			System.out.println("nodes Formatted Correctly");

			line = reader.readLine();
			parts = line.split("\t");			
			int nFlows = Integer.parseInt(parts[1]);			
			for(int i=0; i<nFlows; i++){
				line = reader.readLine();
				parts = line.split("\t");				
				flows.put(Integer.parseInt(parts[0]), new Flow(Integer.parseInt(parts[0]), Main.ID, Integer.parseInt(parts[1]), Configurations.PACKET_RATE));
			}
			System.out.println("flows Formatted Correctly");

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
			if(Main.verbose){
				System.out.println("CONTROL: Received " + nShadowQueueReceived + " shadow-queues for iteration " + Main.iteration);
			}
			if(nShadowQueueReceived == Main.neighbors.size()){

				receivedShadowQueueLock.notifyAll();
			}
		}

	}

	public static void notifyShadowPacketArrival(){
		synchronized(receivedShadowPacketLock){
			nShadowPacketReceived++;
			if(Main.verbose){
				System.out.println("CONTROL: Received " + nShadowPacketReceived + " shadow-packets for iteration " + Main.iteration);
			}
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

	public static String getRealQueues(){
		String ret = "";
		Iterator<Integer> iterator = neighbors.keySet().iterator();
		while(iterator.hasNext()){
			int neighbor = iterator.next();
			ret += neighbor + ":" + neighbors.get(neighbor).realQueue.size() + ", ";
		}
		return ret;
	}

	public static HashMap<Integer, ShadowQueue> getShadowQueues(){
		//TODO: Do we need locking here - synchronized with Main.updateShadowQueue(dest, change) ?
		//TODO: Should we send the reference to original or should we return a copy (expensive) ?
		return Main.shadowQueues;
	}

	public static void updateShadowQueue(ControlPacket packet, int link){
		if(packet.type!=Configurations.SHADOW_PACKET_TYPE){
			System.out.println("ERROR: updateShadowQueue: wrong packet " + packet);
			return;
		}

		if(packet.shadowPackets.size() > 1){
			if(Main.verbose){
				System.out.println("CONTROL: INFO: Proportional splitting is ON");
			}
		}

		synchronized(shadowQueueLock){
			Iterator<Integer> iterator = packet.shadowPackets.keySet().iterator();			
			while(iterator.hasNext()){
				int destination = iterator.next();
				int nPackets = packet.shadowPackets.get(destination);				
				Main.shadowPacketsReceived += nPackets;
				
				synchronized(Main.lastLock){
					HashMap<Integer, Integer> temp = Main.shdwRecv.get(destination);
					temp.put(link, temp.get(link) + nPackets);
					Main.shdwRecv.put(destination, temp);
				}
				
				if(Main.DEBUG){
					synchronized(Main.lastLock){
						synchronized(Main.lastShadowPacketsReceivedLock){
							Main.lastShadowPacketsReceived += nPackets;
						}
					}
				}
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

	public static void updateShadowQueueDropped(int destination){
		if(destination==Main.ID){
			return;
		}
		synchronized(shadowQueueLock){
			if(shadowQueues.containsKey(destination)){
				shadowQueues.get(destination).update(-1);
			}	
		}
		synchronized(Main.lastLock){
			synchronized(Main.shdwDropLock){
				Main.shdwDrop.put(destination, Main.shdwDrop.get(destination) + 1);
			}
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
				
				Main.shadowPacketsGenerated += nShadowPackets;

			}
		}
		
		synchronized(Main.lastLock){
			synchronized(Main.shdwGenLock){
				Main.shdwGen.put(destination, Main.shdwGen.get(destination) + nShadowPackets);
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
		if(Configurations.TURN_OFF_FLOWS){
			return;
		}		
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
