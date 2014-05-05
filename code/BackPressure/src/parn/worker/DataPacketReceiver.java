package parn.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import parn.main.Configurations;
import parn.main.Main;
import parn.node.Neighbor;
import parn.packet.ControlPacket;
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
		try {
			while(!Configurations.SYSTEM_HALT){
				//System.out.println(this + " is running " + Configurations.SYSTEM_HALT);

				DataPacket packet = (DataPacket) connection.readObject();
				//CheckValidity: Check and decrement TTL before putting packet in input buffer 
				if(packet.checkValidity()){
					Main.inputBuffer.put(packet);
					synchronized(Main.inputBufferSizeLock){
						Main.inputBufferSize++;
					}
					if(Main.DEBUG){
						synchronized(Main.lastLock){
							synchronized(Main.lastDataPacketsReceivedLock){
								Main.lastDataPacketsReceived++;
							}
						}
					}
					synchronized(Main.dataPacketsReceivedLock){
						Main.dataPacketsReceived++;
					}
				}else{					
					Main.updateShadowQueueDropped(packet.destination);
					synchronized(Main.dataPacketsDroppedLock){
						Main.dataPacketsDropped++;
					}
				}
				if(Main.verbose) {
					System.out.println("DATA: " + this + ": received " + packet);
				}





			}
		}catch(Throwable e){
			e.printStackTrace();
			System.out.println(this + " FATAL ERROR " + e.getMessage());
			Configurations.FATAL_ERROR = true;
		}

	}

	public String toString(){
		return "DataReceiver["+neighbor+"]";
	}
}
