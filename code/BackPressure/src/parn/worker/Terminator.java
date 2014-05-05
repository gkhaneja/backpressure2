package parn.worker;

import parn.main.Configurations;
import parn.main.Main;

public class Terminator extends Thread {
	public void run(){
		while(true){
			if (System.currentTimeMillis() - Main.startTime > Main.duration || Configurations.FATAL_ERROR){
				Configurations.SYSTEM_HALT = true;
				//System.out.println("INFO: Stopping the system");
				CommandPromt.printStats();
				System.exit(0);
				
			}
			try{
				sleep(1000);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
