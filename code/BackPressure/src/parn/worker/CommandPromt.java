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
				//int flowId = Integer.parseInt(parts[1]);
				//Main.flows.get(flowId).stop();
			}
		}
	}
	
	public  static void printStats(){
		System.out.println("STAT: time: " + (System.currentTimeMillis() - Main.startTime));
		System.out.println("STAT: The system encountered an error. " + Main.error);
		System.out.println("STAT: No. of iterations: " + Main.iteration );
		System.out.println("STAT: Stable iteration: " + Configurations.stableIterations);
		System.out.println("STAT: Stable time: " + Configurations.stableTime);
		System.out.println();
		
		System.out.println("STAT: shadow packets generated: " + Main.shadowPacketsGenerated);
		System.out.println("STAT: Extra shadow packets generated:  " + Main.extraShadowPacketsGenerated);
		System.out.println("STAT: shadow packets sent:  " + Main.shadowPacketsSent);
		System.out.println("STAT: shadow packets received:  " + Main.shadowPacketsReceived);
		System.out.println();

		System.out.println("STAT: Control Bytes sent: " + Main.controlBytesSent);
		System.out.println("STAT: Control Packets sent : " + Main.controlPacketsSent);
		System.out.println("STAT: Control bytes received: " + Main.controlBytesReceived);
		System.out.println("STAT: Control packets received: " + Main.controlPacketsReceived);
		System.out.println();
		
		System.out.println("STAT: data packets generated: " + Main.dataPacketsGenerated);
		System.out.println("STAT: data packets sent:  " + Main.dataPacketsSent);
		System.out.println("STAT: data packets received:  " + Main.dataPacketsReceived);
		System.out.println();
		
		Iterator<Integer> iterator = Main.flowStatReceived.keySet().iterator();
		while(iterator.hasNext()){
			System.out.println("flow stats");
			System.out.println(Main.flowStatReceived.get(iterator.next()));
			
		}
	}
}
