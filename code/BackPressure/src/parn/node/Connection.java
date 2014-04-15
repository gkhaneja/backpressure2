package parn.node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Connection {
	public ObjectInputStream in;
	public ObjectOutputStream out;
	public String type;
	
	public Connection(ObjectInputStream in, ObjectOutputStream out){
		this.in = in;
		this.out = out;
	}
	
	public Connection(){
		in=null;
		out=null;
	}
	
	

}
