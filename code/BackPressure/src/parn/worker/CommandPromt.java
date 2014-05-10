package parn.worker;

import java.util.Iterator;
import java.util.Scanner;

import parn.main.Configurations;
import parn.main.FlowStat;
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
		try{
			
			long stopTime = System.currentTimeMillis();
			double duration = (stopTime - Main.startTime) / 1000.0;
			System.out.println("STAT: Total time: " + (stopTime - Main.startTime));
			System.out.println("STAT: The system encountered an error. " + Main.error);
			System.out.println("STAT: No. of iterations: " + Main.iteration );
			System.out.println("STAT: Stable iteration: " + Configurations.stableIterations);
			System.out.println("STAT: Stable time: " + Configurations.stableTime);
			System.out.println("STAT:");

			
			
			
			System.out.println("STAT: shadow packets generated: " + Main.shadowPacketsGenerated);
			System.out.println("STAT: Extra shadow packets generated:  " + Main.extraShadowPacketsGenerated);
			System.out.println("STAT: shadow packets sent:  " + Main.shadowPacketsSent);
			System.out.println("STAT: shadow packets received:  " + Main.shadowPacketsReceived);
			System.out.println("STAT:");

			System.out.println("STAT: Control Bytes sent: " + Main.controlBytesSent);
			System.out.println("STAT: Control Packets sent : " + Main.controlPacketsSent);
			System.out.println("STAT: Control bytes received: " + Main.controlBytesReceived);
			System.out.println("STAT: Control packets received: " + Main.controlPacketsReceived);
			System.out.println("STAT:");

			System.out.println("STAT: data packets generated: " + Main.dataPacketsGenerated);
			System.out.println("STAT: data packets sent:  " + Main.dataPacketsSent);
			System.out.println("STAT: data packets received:  " + Main.dataPacketsReceived);
			System.out.println("STAT: data packets dropped:  " + Main.dataPacketsDropped);
			System.out.println("STAT:");

			System.out.println("Probs");
			Iterator<Integer> iterator2 = Main.nodes.keySet().iterator();
			while(iterator2.hasNext()){
				System.out.print("STAT: "); Main.nodes.get(iterator2.next()).printProbs();
			}
			System.out.println();
			
			long bytesConsumed=0;
			double avgPathLength=0;
			double avgPathExpantion=0;
			long count=0;
			int maxPathLength=0;
			
			Iterator<Integer> iterator = Main.flowStatReceived.keySet().iterator();
			while(iterator.hasNext()){
				//System.out.println("flow stats");
				int flowId = iterator.next();
				FlowStat flowStat = Main.flowStatReceived.get(flowId);
				bytesConsumed += flowStat.nBytes;
				avgPathLength += flowStat.nPackets*flowStat.pathLength;
				avgPathExpantion += flowStat.nPackets*flowStat.pathExpansion;
				count += flowStat.nPackets;
				if(flowStat.maxPathLength > maxPathLength){
					maxPathLength = flowStat.maxPathLength;
				}
				System.out.println(flowStat);

			}
			System.out.println();
						
			avgPathLength = avgPathLength / count;
			avgPathExpantion = avgPathExpantion / count;
			double goodput = bytesConsumed / duration;
			
			double controlOverheadBytes = (Main.controlBytesReceived + Main.controlBytesSent) / (2 * Main.neighbors.size() * duration);
			double controlOverheadPackets = (Main.controlPacketsReceived+ Main.controlPacketsSent) / (2 * Main.neighbors.size() * duration);
			double throughput = (Main.dataPacketsReceived + Main.dataPacketsSent)*Main.dataPacketSize / (2 * Main.neighbors.size() * duration);
			
			System.out.println("STAT: generation (bytes / second)      : " + Configurations.PACKET_RATE*Main.dataPacketSize*Main.flows.size());
			System.out.println("STAT: goodput (bytes / second)         : " + goodput);
			System.out.println("STAT: throughput (bytes / second)      : " + throughput);
			System.out.println("STAT: control overhead (bytes / second): " + controlOverheadBytes);
			System.out.println("STAT: control overhead (pckts / second): " + controlOverheadPackets);
			System.out.println("STAT: Total avgPathLength:                   : " + avgPathLength);
			System.out.println("STAT: Total avgPathExpansion:                : " + avgPathExpantion);
			System.out.println("STAT: Total maxPathLength:                   : " + maxPathLength);
		
		}catch(Throwable e){
			e.printStackTrace();
			System.out.println("STAT: FATAL ERROR: " + e.getMessage());
			Configurations.FATAL_ERROR = true;
		}
	}
}
