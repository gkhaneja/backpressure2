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
			
			synchronized(Main.iterationPhaseLock){
				while(Main.iterationPhase==0){
					try {
						Main.iterationPhaseLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}						
			}
			
			if(Configurations.DEBUG_ON){
				try{
					System.out.println("Sleeping");
					sleep(Configurations.SLOW_DOWN_FACTOR);
				}catch(InterruptedException e){
					System.out.println("CONTROL: " + this + " got interrupted");
				}
			}

			System.out.println("CONTROL: " + this + " Starting " + Main.iteration + ", " + Main.iterationPhase);

			
			ControlPacket packet = new ControlPacket(Main.ID, neighbor.node.id, Configurations.SHADOW_QUEUE_TYPE, Main.iteration, Main.iterationPhase);
			try {
				System.out.println("CONTROL: " + this + " sending " + packet);
				connection.writeObject(packet);
				Main.updateControlSenderStats(packet);
			} catch (IOException e) {
				System.out.println("CONTROL: " + this + ": Error sending " + packet);
				e.printStackTrace();
				break;
			}
			
			
			synchronized(Main.sentShadowQueueLock){
				Main.nShadowQueuesSent++;
				System.out.println("CONTROL: " + this + " nShadowQueuesSent: " + Main.nShadowQueuesSent + ", NeighborSize: " + Main.neighbors.size());
				if(Main.nShadowQueuesSent == Main.neighbors.size()){
					System.out.println("CONTROL: " + this + " The Last ControlPacketSender");
					Main.nShadowQueuesSent=0;
					
					synchronized(Main.receivedShadowQueueLock){
						while(Main.nShadowQueueReceived != Main.neighbors.size()){
							try{
								Main.receivedShadowQueueLock.wait();
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						Main.nShadowQueueReceived=0;
					}
					
					synchronized(Main.iterationPhaseLock){
						//System.out.println("CONTROL: " + this + " Starting " + Main.iteration + ", " + Main.iterationPhase);
						Main.iterationPhase=0;
						Main.iterationPhaseLock.notifyAll();					
					}
				}
				
				
				
			}
			
			synchronized(Main.iterationPhaseLock){
				while(Main.iterationPhase==1){
					try {
						Main.iterationPhaseLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}						
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
		return "ControlSender["+neighbor+"]";
	}
}
