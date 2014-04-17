package parn.worker;

import java.io.IOException;
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
		while(true){
			//System.out.println(this + " is running");
			/*try{
				sleep(5000);
			}catch(InterruptedException e){
				System.out.println(this + " got interrupted");
			}*/
			//TODO: Receiver shouldn't sleep, right ?
			try {
				ControlPacket packet = (ControlPacket) connection.readObject();
				System.out.println(this + " received " + packet);
				if(packet.type == Configurations.SHADOW_QUEUE_TYPE){
					neighbor.updateShadowQueue(packet);
				}else { 
					Main.updateShadowQueue(packet);
				}
			} catch (ClassNotFoundException | IOException e) {
				System.out.println(this + " Error receving data packets");
				//TODO: perhaps remove stack trace
				e.printStackTrace();
			}
		}
	}
	
	public String toString(){
		return "ControlReceiver["+neighbor+"]";
	}
}
