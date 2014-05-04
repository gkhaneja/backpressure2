package parn.worker;

import java.io.IOException;
import java.io.ObjectOutputStream;

import parn.main.Configurations;
import parn.main.Main;
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
		try{
			//System.out.println(this + " is starting.");
			while(!Configurations.SYSTEM_HALT){
				//System.out.println(this + " is running");
				if(Configurations.DEBUG_ON){
						System.out.println("DATA: " + this + " Sleeping");
						sleep(Configurations.SLOW_DOWN_FACTOR);
					
				}

				DataPacket packet = neighbor.realQueue.poll();
				if(packet==null) continue;
				
					//System.out.println("DATA: " + this + " sending " + packet);
					connection.writeObject(packet);
				 

				synchronized(Main.dataPacketsSentLock){
					Main.dataPacketsSent++;
				}
				synchronized(Main.lastSecondDataPacketsSentLock){
					Main.lastSecondDataPacketsSent++;
				}
			}
		}catch(Throwable e){
			e.printStackTrace();
			System.out.println(this + " FATAL ERROR " + e.getMessage());
			Configurations.FATAL_ERROR = true;
		}
		
	}

	public String toString(){
		return "DataSender["+neighbor+"]";
	}
}
