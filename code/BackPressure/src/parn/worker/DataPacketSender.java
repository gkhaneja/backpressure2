package parn.worker;

import java.io.IOException;
import java.io.ObjectOutputStream;

import parn.main.Configurations;
import parn.node.Neighbor;
import parn.packet.DataPacket;

public class DataPacketSender extends Thread {	
	Neighbor neighbor;
	ObjectOutputStream connection;

	public DataPacketSender(Neighbor neighbor, ObjectOutputStream connection){
		this.neighbor = neighbor;
		this.connection = connection;
	}

	public void run(){
		//System.out.println(this + " is starting.");
		while(!Configurations.SYSTEM_HALT){
			//System.out.println(this + " is running");
			if(Configurations.DEBUG_ON){
				try{
					System.out.println("DATA: " + this + " Sleeping");
					sleep(Configurations.SLOW_DOWN_FACTOR);
				}catch(InterruptedException e){
					System.out.println("DATA: " + this + " got interrupted");
				}
			}

			DataPacket packet = neighbor.realQueue.poll();
			if(packet==null) continue;
			try {
				//System.out.println("DATA: " + this + " sending " + packet);
				connection.writeObject(packet);
			} catch (IOException e) {
				System.out.println("DATA: " + this + ": Error sending " + packet);
				e.printStackTrace();
				System.out.println(e.getMessage());
				
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
		return "DataSender["+neighbor+"]";
	}
}
