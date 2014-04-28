package parn.worker;

import java.io.ObjectInputStream;

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
		//System.out.println(this + " is starting.");
		while(!Configurations.SYSTEM_HALT){
			//System.out.println(this + " is running");
			try {
				ControlPacket packet = (ControlPacket) connection.readObject();
				//System.out.println("Size of object: " + Main.sizeof(packet));
				//TODO: Inefficient step - 
				Main.updateControlReceiverStats(packet);
				System.out.println(this + " received " + packet);
				if(packet.type == Configurations.SHADOW_QUEUE_TYPE){
					neighbor.updateShadowQueue(packet);
				}else { 
					Main.updateShadowQueue(packet);
				}
			} catch (Exception e) {
				System.out.println(this + " Error receving data packets");
				e.printStackTrace();
				break;
			}
		}
	}
	
	public String toString(){
		return "ControlReceiver["+neighbor+"]";
	}
}
