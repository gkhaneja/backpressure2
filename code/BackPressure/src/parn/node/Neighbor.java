package parn.node;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import parn.main.Configurations;
import parn.main.Main;
import parn.packet.ControlPacket;
import parn.packet.DataPacket;
import parn.packet.ShadowQueue;
import parn.worker.ControlPacketReceiver;
import parn.worker.ControlPacketSender;
import parn.worker.DataPacketReceiver;
import parn.worker.DataPacketSender;


public class Neighbor extends Thread {
	
	//Following node is the neighbor with the following port
	public Node node;
	int port;
	
	//connections over which packets are sent
	Connection control;
	Connection data;
	Socket dataSocket;
	Socket controlSocket;
	
	//Connection workers: each worker handle send/receive of data/control packets to *this* neighbor. Look at respective threads for details
	DataPacketSender dataSender;
	ControlPacketSender controlSender;
	DataPacketReceiver dataReceiver;
	ControlPacketReceiver controlReceiver;
	
	//RealQueue for this neighbor. Accessed by DataPacketSender and Router threads 
	public LinkedBlockingQueue<DataPacket> realQueue;
	
	//Lengths of shadowQueues for each destination for each neighbor are maintained here
	public HashMap<Integer, ShadowQueue> shadowQueues;
	

	//public static void main(String[] args){
		//ServerSocket listener
	//}
	
	public Neighbor(int port){
		this.port = port;
		realQueue = new LinkedBlockingQueue<DataPacket>();
		shadowQueues = new HashMap<Integer, ShadowQueue>();
	}
	
	public Neighbor(Node node, int port){
		this.node=node;
		this.port = port;
		realQueue = new LinkedBlockingQueue<DataPacket>();
		shadowQueues = new HashMap<Integer, ShadowQueue>();

	}
	
	//TODO: No need to initialize neighbors shadow queue, right ? 
	//public initializeShadowQueues(){
	//}
	
	public String toString(){
		return "Neighbor["+node.id+","+port+"]";
	}

	//TODO: Contains sleep. Make sure it's Executed in separate thread 
	//Algo: 
	
	public void updateShadowQueue(ControlPacket packet){
		Object lock = new Object();
		//TODO: Lock not required because there's only one thread updating this neighbors shadow queues.
		synchronized(lock){
			//TODO: Should we send updates or all Shadow queue (tradeoff - packet size vs number of control packets) ? Perhaps, batch updates ?
			shadowQueues = packet.shadowQueues;
		}
	}
	
	public void run(){
		control = new Connection();
		controlSocket= new Socket();
		if(!createConnection(control, controlSocket)){
			//TODO: Error handling on connection failure
			return;
		}
		
		data = new Connection();
		dataSocket= new Socket();
		if(!createConnection(data, dataSocket)){
			//TODO: Error handling on connection failure
			return;
		}
		
		System.out.println("Starting threads");
		controlReceiver = new ControlPacketReceiver(this, control.in);
		controlSender = new ControlPacketSender(this, control.out);
		controlReceiver.start();
		controlSender.start();
		
		dataReceiver = new DataPacketReceiver(this, data.in);
		dataSender = new DataPacketSender(this, data.out);
		dataReceiver.start();
		dataSender.start();
		
		try {
			dataReceiver.join();
			dataSender.join();
			controlSender.join();
			controlReceiver.join();
		} catch (InterruptedException e) {
			System.out.println(this + " got interrupted. Closing sockets");
			e.printStackTrace();
		}
		try {
			dataSocket.close();
			controlSocket.close();
		} catch (IOException e) {
			System.out.println(this + " cannot close sockets.");
			e.printStackTrace();
		}
		
	}

	boolean createConnection(Connection connection, Socket socket){
		try {
			if(node.id <= Main.ID){
				int ntries=0;
				//boolean connected = false;
				while(ntries++ < Configurations.MAX_CONN_ATTEMTPS){
					System.out.println("Trying to connect to " + this);
					try{
						socket = new Socket(node.address,port);//, Main.nodes.get(Main.ID).address, Configurations.getAvailablePort());
						//connected = true;
						if(socket.isBound()) break;
					}catch(IOException e){
						e.printStackTrace();
					}
					try {
					    Thread.sleep(Configurations.CONN_ATTEMPT_SLEEP_TIME);
					} catch(InterruptedException ex) {
						System.out.println("Got interrupted");
					    Thread.currentThread().interrupt();
					}
				}
				if(!socket.isBound()){
					System.out.println(this + " cannot be connected");
					
				}
				connection.out = new ObjectOutputStream(socket.getOutputStream());
				connection.out.flush();
				connection.in = new ObjectInputStream(socket.getInputStream());
			}else{
				System.out.println("Waiting for connection from " + this);
				ServerSocket serverSocket = new ServerSocket(port, Configurations.BACKLOG, Main.nodes.get(Main.ID).address);
				socket = serverSocket.accept();		
				serverSocket.close();
				connection.in = new ObjectInputStream(socket.getInputStream());
				connection.out = new ObjectOutputStream(socket.getOutputStream());
				connection.out.flush();
			}
		} catch (IOException e) {
			System.out.println(this + " cannot be connected");
			e.printStackTrace();
			return false;
			
		}
		System.out.println(this + " is connected");
		/*try {
			System.out.println("Waiting for streams from " + this);
			connection.in = new ObjectInputStream(socket.getInputStream());
			connection.out = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("Got streams from " + this);
		} catch (IOException e) {
			System.out.println(this + " cannot be connected");
			return false;
			//e.printStackTrace();
		}*/
		return true;

	}

}