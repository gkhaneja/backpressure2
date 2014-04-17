package parn.main;

public class Configurations {
	public static final int MAX_CONN_ATTEMTPS=2;
	public static final int CONN_ATTEMPT_SLEEP_TIME=1000;
	public static final int BACKLOG=10;
	public static final int MAX_HOP = 30;
	public static final int CONTROL_PACKET_INTERVAL = 5000;
	public static final int SHADOW_QUEUE_TYPE=1;
	public static final int SHADOW_PACKET_TYPE=2;
	
	//public static final int CONN_TIMEOUT=
	private static int nextAvailablePort = 10000;
	public static synchronized int getAvailablePort(){
		nextAvailablePort++;
		return nextAvailablePort;
	}
}
