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
		while(!Configurations.SYSTEM_HALT){
			//System.out.println(this + " is running");
			try {
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
					System.out.println("CONTROL: " + this + " received " + packet);
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
					System.out.println("CONTROL: " + this + " received " + packet);
					Main.updateShadowQueue(packet);
					
				}
			} catch (Exception e) {
				System.out.println("CONTROL: " + this + " Error receving data packets");
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
		return "ControlReceiver["+neighbor+"]";
	}
}
