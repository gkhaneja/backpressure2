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
		while(true){
			//System.out.println(this + " is running");
			if(Configurations.DEBUG_ON){
				try{
					sleep(Configurations.SLOW_DOWN_FACTOR);
				}catch(InterruptedException e){
					System.out.println(this + " got interrupted");
				}
			}

			DataPacket packet = neighbor.realQueue.poll();
			if(packet==null) continue;
			try {
				connection.writeObject(packet);
			} catch (IOException e) {
				System.out.println(this + ": Error sending " + packet);
				e.printStackTrace();
				break;
			}
		}
	}

	public String toString(){
		return "DataSender["+neighbor+"]";
	}
}
