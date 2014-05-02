package parn.worker;

import java.util.ArrayList;
import java.util.Random;

import parn.main.Configurations;
import parn.main.Main;
import parn.packet.DataPacket;

public class Flow extends Thread {
	
	// TODO: Bounded flows. Currently, runs for infinite time...
	int id;
	//rate - packets per seconds
	int rate;
	int source;
	int destination;
	int sequenceNumber;
	
	public Flow(int id, int source, int destination, int rate){
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
			 
			
				DataPacket packet = new DataPacket(id, source, destination, sequenceNumber, sequenceNumber + rate - 1, true);
				sequenceNumber += rate;

				try {
					Main.inputBuffer.put(packet);
					//System.out.println("DATA: Generated " + packet);				
				} catch (InterruptedException e) {
					System.out.println("DATA: " + this + " Error adding packet to input buffer");
					e.printStackTrace();
				}
				int fraction = (int) (rate*Main.epsilon);
				int nShadowPackets = rate + fraction;
				double prob = rate*Main.epsilon - (double) fraction;
				
				//System.out.println("DATA : fraction " + fraction + " prob " + prob + " nShadowPackets " + nShadowPackets + " rate " + rate);
								
				if(rand.nextDouble() < prob){
					nShadowPackets++;
				}
				
				Main.addShadowPackets(destination, nShadowPackets);
				
				synchronized(Main.dataPacketStatLock){
					Main.dataPacketsGenerated+= rate;
				}
			
			
			while(System.currentTimeMillis() - startTime < 1000){ }
			startTime = System.currentTimeMillis();
		}
	}
	
	
	public String toString(){
		return "FlowGenerator";
	}
}
