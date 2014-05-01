package parn.worker;

import java.util.Random;

import parn.main.Configurations;
import parn.main.Main;
import parn.packet.DataPacket;

public class Flow extends Thread {
	
	// TODO: Bounded flows. Currently, runs for infinite time...
	int id;
	//rate - packets per seconds
	double rate;
	int source;
	int destination;
	int sequenceNumber;
	
	public Flow(int id, int source, int destination, double rate){
		this.id = id;
		this.source = source;
		this.destination = destination;
		this.rate = rate;
		this.sequenceNumber=0;
	}
	
	
	public void run(){
		Random rand = new Random();
		long startTime = System.currentTimeMillis();
		double frequency =   1.0/rate;
		//System.out.println("Sleep time is " + sleepTime);
		while(!Configurations.SYSTEM_HALT){
			
			/*try{
				sleep((long) sleepTime*1000);
			}catch(InterruptedException e){
				System.out.println(this + " got interrupted");
			}*/
			
			//TODO: Add data and shadow packet in batches

			for(int i=0;i<rate; i++){
				DataPacket packet = new DataPacket(id, source, destination, sequenceNumber++);

				try {
					Main.inputBuffer.put(packet);
					//System.out.println("DATA: Generated " + packet);
					if(rand.nextDouble() < Main.epsilon){
						Main.addShadowPackets(destination, 2);	
					}else{
						Main.addShadowPackets(destination, 1);
					}
					synchronized(Main.dataPacketStatLock){
						Main.dataPacketsGenerated++;
					}
				} catch (InterruptedException e) {
					System.out.println("DATA: " + this + " Error adding packet to input buffer");
					e.printStackTrace();
				}
			}
			
			while(System.currentTimeMillis() - startTime < 1000){ }
			startTime = System.currentTimeMillis();
		}
	}
	
	
	public String toString(){
		return "FlowGenerator";
	}
}
