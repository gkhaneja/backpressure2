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
	Random rand = new Random();

	public Flow(int id, int source, int destination, int rate){
		this.id = id;
		this.source = source;
		this.destination = destination;
		this.rate = rate;
		this.sequenceNumber=0;
	}


	public void run(){
		
		try{
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

				
					Main.inputBuffer.put(packet);
					//System.out.println("DATA: Generated " + packet);				
				
				int intPart = (int) (rate*Main.epsilon);
				int nShadowPackets = rate + intPart;
				double probLimit = rate*Main.epsilon - (double) intPart;
				int extraPacketAdded=0;


				if(rand.nextDouble() < probLimit){
					nShadowPackets++;
					extraPacketAdded=1;
				}
				System.out.println("DATA: " + this + ": intPart " + intPart + " prob " + probLimit + " nShadowPackets " + nShadowPackets + " rate " + rate);
				Main.addShadowPackets(destination, nShadowPackets);

				synchronized(Main.dataPacketStatLock){
					Main.dataPacketsGenerated+= rate;
				}

				synchronized(Main.extraShadowPacketGeneratedLock){
					Main.extraShadowPacketsGenerated += intPart + extraPacketAdded;
				}

				String str = this + " generated packets at " + (System.currentTimeMillis() - Main.startTime);
				//while(System.currentTimeMillis() - startTime < 1000){ }
				
					if(1000 - System.currentTimeMillis()  + startTime > 0){
						sleep(1000 - System.currentTimeMillis()  + startTime);
					}
				
				System.out.println(str + ". Starting again at " + (System.currentTimeMillis() - Main.startTime));				
				startTime = System.currentTimeMillis();
			}
		}catch(Throwable e){
			e.printStackTrace();
			System.out.println(this + " FATAL ERROR " + e.getMessage());
			Configurations.FATAL_ERROR = true;
		}
	}


	public String toString(){
		return "FlowGenerator[" + destination + "]";
	}
}
