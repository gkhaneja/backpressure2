package parn.worker;

import java.util.Random;

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
		int sleepTime =  (int) ((int) 1.0/rate);
		//System.out.println("Sleep time is " + sleepTime);
		while(true){
			//TODO: Add randomness according to conf file
			try{
				sleep(sleepTime*1000);
			}catch(InterruptedException e){
				System.out.println(this + " got interrupted");
			}
			DataPacket packet = new DataPacket(id, source, destination, sequenceNumber++);
			try {
				Main.inputBuffer.put(packet);
				System.out.println("Generated " + packet);
				//TODO: Add Shadow Packets
				if(rand.nextDouble() < Main.epsilon){
					Main.addShadowPackets(destination, 2);
				}else{
					Main.addShadowPackets(destination, 1);
				}
			} catch (InterruptedException e) {
				System.out.println(this + " Error adding packet to input buffer");
				e.printStackTrace();
			}
		}
	}
	
	
	public String toString(){
		return "FlowGenerator";
	}
}
