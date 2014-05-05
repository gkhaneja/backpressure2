package parn.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import parn.main.Configurations;
import parn.main.Main;
import parn.node.Neighbor;
import parn.packet.ControlPacket;

public class ControlPacketReceiver extends Thread {
	Neighbor neighbor;
	ObjectInputStream connection;

	public ControlPacketReceiver(Neighbor neighbor, ObjectInputStream connection){
		this.neighbor = neighbor;
		this.connection = connection;
	}

	public void run(){
		try{
			//System.out.println(this + " is starting.");
			while(!Configurations.SYSTEM_HALT){
				//System.out.println(this + " is running");

				ControlPacket packet = (ControlPacket) connection.readObject();


				Main.updateControlReceiverStats(packet);


				if(packet.type == Configurations.SHADOW_QUEUE_TYPE){

					synchronized (Main.iterationPhaseLock) {
						while(Main.iterationPhase == 0){
							try{
								Main.iterationPhaseLock.wait();
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}
					if(Main.verbose){
						System.out.println("CONTROL: " + this + " received " + packet);
					}
					neighbor.updateShadowQueue(packet);
					
					

				}else { 

					synchronized (Main.iterationPhaseLock) {
						while(Main.iterationPhase == 1){
							try{
								Main.iterationPhaseLock.wait();
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}
					if(Main.verbose){
						System.out.println("CONTROL: " + this + " received " + packet);
					}
					Main.updateShadowQueue(packet, neighbor.node.id);
					
					/*HashMap<Integer, Integer> temp = Main.toknRecv.get(id);
					temp.put(smallestTokenBucket, temp.get(smallestTokenBucket) + 1);
					Main.toknRecv.put(id, temp);*/

				}

			}
		
		}catch(Throwable e){
			e.printStackTrace();
			System.out.println(this + " FATAL ERROR " + e.getMessage());
			Configurations.FATAL_ERROR = true;
		}

	}

	public String toString(){
		return "ControlReceiver["+neighbor+"]";
	}
}
