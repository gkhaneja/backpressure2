package parn.main;

import java.util.HashMap;
import java.util.Iterator;

public class Configurations {
	
	//constants
	public static final int MAX_CONN_ATTEMTPS=2;
	public static final int CONN_ATTEMPT_SLEEP_TIME=1000;
	public static final int BACKLOG=10;
	public static final int MAX_HOP = 30;
	public static final int CONTROL_PACKET_INTERVAL = 5000;
	public static final int SHADOW_QUEUE_TYPE=1;
	public static final int SHADOW_PACKET_TYPE=2;
	public static final int PAYLOAD_SIZE=600;
	
	//Switches
	//public static final boolean TURN_OFF_DATA_CHANNELS = true;
	public static final boolean TURN_OFF_FLOWS = false;
	public static final boolean DEBUG_ON = true;
	public static final int SLOW_DOWN_FACTOR = 5000;
	public static boolean SYSTEM_HALT = false;
	
	
	//Measurements
	public static  boolean isStable = false;
	public static  int stableIterations = 0;
	public static  long stableTime = 0;
	public static long startTime = System.currentTimeMillis();
	
	
	
	//PORTS
	private static int nextAvailablePort = 10000;
	public static synchronized int getAvailablePort(){
		nextAvailablePort++;
		return nextAvailablePort;
	}
	
	//Utilities
	public static <K,V> String hashToString(HashMap<K,V> map){
		String ret="[";
		Iterator<K> iterator = map.keySet().iterator();
		int first=1;
		while(iterator.hasNext()){
			K key = iterator.next();
			V value = map.get(key);
			if(first==0){
				ret += ",";
			}else{
				first=0;
			}
			ret += key + ":" + value;
			
		}
		ret+="]";
		return ret;
	}
	
	
}
