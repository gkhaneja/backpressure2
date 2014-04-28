package parn.worker;

import java.io.IOException;
import java.io.ObjectInputStream;

import parn.main.Configurations;
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
	
	public void run(){
		//System.out.println(this + " is starting.");
		while(!Configurations.SYSTEM_HALT){
			//System.out.println(this + " is running " + Configurations.SYSTEM_HALT);
			try {
				DataPacket packet = (DataPacket) connection.readObject();
				//CheckValidity: Check and decrement TTL before putting packet in input buffer 
				if(packet.checkValidity()) Main.inputBuffer.put(packet);
				System.out.println("DATA: " + this + ": received " + packet);

			} catch (IOException e) {
				System.out.println("DATA: " + this + " Error receving data packets");
				//e.printStackTrace();
				break;
			} catch (InterruptedException e) {
				System.out.println("DATA: " + this + " Error adding packet to input buffer");
				//e.printStackTrace();
				break;
			}catch (ClassNotFoundException e) {
				System.out.println("DATA: " + this + " Error adding packet to input buffer");
				//e.printStackTrace();
				break;
			}
			
		
		}
		try {
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String toString(){
		return "DataReceiver["+neighbor+"]";
	}
}
