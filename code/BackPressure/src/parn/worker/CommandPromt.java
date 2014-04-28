package parn.worker;

import java.util.Iterator;
import java.util.Scanner;

import parn.main.Configurations;
import parn.main.Main;

public class CommandPromt extends Thread {
	
	
	public void run(){
		Scanner scanner = new Scanner(System.in);
		while(scanner.hasNext()){
			String line = scanner.next();
			String parts[] = line.split(" ");
			String command = parts[0];
			if(command.equalsIgnoreCase("stop")){
				Configurations.SYSTEM_HALT = true;				
				printStats();
			}else if(command.equalsIgnoreCase("kill")){
				int flowId = Integer.parseInt(parts[1]);
				Main.flows.get(flowId).stop();
			}
		}
	}
	
	public  static void printStats(){
		System.out.println("No. of iterations: " + Main.shadowPacketGenerator.iteration );
		System.out.println("Stable iteration: " + Configurations.stableIterations);
		System.out.println("Stable time: " + Configurations.stableTime);
		System.out.println();
		
		System.out.println("shadow packets generated: " + Main.shadowPacketsGenerated);
		System.out.println("Extra shadow packets generated:  " + Main.extraShadowPacketsGenerated);
		System.out.println("shadow packets sent:  " + Main.shadowPacketsSent);
		System.out.println("shadow packets received:  " + Main.shadowPacketsReceived);
		System.out.println();

		System.out.println("Control Bytes sent: " + Main.controlBytesSent);
		System.out.println("Control Packets sent : " + Main.controlPacketsSent);
		System.out.println("Control bytes received: " + Main.controlBytesReceived);
		System.out.println("Control packets sent: " + Main.controlPacketsSent);
		System.out.println();
		
		System.out.println("data packets generated: " + Main.dataPacketsGenerated);
		System.out.println("data packets sent:  " + Main.dataPacketsSent);
		System.out.println("data packets received:  " + Main.dataPacketsReceived);
		System.out.println();
		
		Iterator<Integer> iterator = Main.flowStatReceived.keySet().iterator();
		while(iterator.hasNext()){
			System.out.println(Main.flowStatReceived.get(iterator.next()));
		}
	}
}
