package parn.worker;

import java.io.IOException;
import java.io.ObjectInputStream;

import parn.main.Main;
import parn.node.Neighbor;
import parn.packet.DataPacket;

public class DataPacketReceiver extends Thread {
	Neighbor neighbor;
	ObjectInputStream connection;
	
	public DataPacketReceiver(Neighbor neighbor, ObjectInputStream connection){
		this.neighbor = neighbor;
		this.connection = connection;
	}
	
	//TODO: Handle unexpected connection close
	public void run(){
		//System.out.println(this + " is starting.");
		while(true){
			//System.out.println(this + " is running");
			//TODO: parametrized sleep time for all sender/receiver threads
			try{
				sleep(5000);
			}catch(InterruptedException e){
				System.out.println(this + " got interrupted");
			}
			try {
				DataPacket packet = (DataPacket) connection.readObject();
				//Check and decrement TTL before putting packet in input buffer
				//TODO: Exception giving line - 
				if(packet.checkValidity()) Main.inputBuffer.put(packet);
				System.out.println(this + ": received " + packet);

			} catch (ClassNotFoundException | IOException e) {
				System.out.println(this + " Error receving data packets");
				//TODO: perhaps remove stack trace
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.out.println(this + " Error adding packet to input buffer");
				//TODO: perhaps remove stack trace
				e.printStackTrace();
			}
			
		
		}
	}
	
	public String toString(){
		return "DataReceiver["+neighbor+"]";
	}
}
