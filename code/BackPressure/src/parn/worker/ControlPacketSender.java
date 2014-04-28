package parn.worker;

import java.io.IOException;
import java.io.ObjectOutputStream;

import parn.main.Configurations;
import parn.main.Main;
import parn.node.Neighbor;
import parn.packet.ControlPacket;

public class ControlPacketSender extends Thread{
	Neighbor neighbor;
	ObjectOutputStream connection;
	
	//TODO: When to send control packets. Should there be a trigger - everytime any Main.ShadowQueue length changes, send control packets to all neighbors.
	//TODO: Measure overhead generated by control packets 
	//TODO: Should we send updates or all Shadow queue (tradeoff - packet size vs number of control packets) ? Perhaps, batch updates ?
	
	public ControlPacketSender(Neighbor neighbor, ObjectOutputStream connection){
		this.neighbor = neighbor;
		this.connection = connection;
	}
	
	public void run(){
		//System.out.println(this + " is starting.");
		while(!Configurations.SYSTEM_HALT){
			//System.out.println(this + " is running");
			try{
				sleep(Configurations.CONTROL_PACKET_INTERVAL);
			}catch(InterruptedException e){
				System.out.println(this + " got interrupted");
			}
			
			
			
			ControlPacket packet = new ControlPacket(Main.ID, neighbor.node.id, Configurations.SHADOW_QUEUE_TYPE);
			try {
				connection.writeObject(packet);
				Main.updateControlSenderStats(packet);
			} catch (IOException e) {
				System.out.println(this + ": Error sending " + packet);
				//TODO: comment stack trace, may be
				e.printStackTrace();
				break;
			}
			
			System.out.println("DEBUG: " + this + " Waiting...");
			//TODO: Should we have event listening model ? or should we keep sending it at regular intervals ? I think it should be based on updates (event). But keeping latter for now.
			//solution: Having it based on notifications.
			try {
				synchronized(Main.shadowQueueSendingNotification){
					Main.shadowQueueSendingNotification.wait();
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public String toString(){
		return "ControlSender["+neighbor+"]";
	}
}
