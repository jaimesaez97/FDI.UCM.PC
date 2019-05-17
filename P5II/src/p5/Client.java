package p5;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	private String _name;
	private String _IPadd;
	private Socket _s;
	private BufferedReader _br;
	private BufferedWriter _bw;
	private DataOutputStream _out;
	
	public Client(String n, String add) {
		try {
			this._name = n;
			this._IPadd = add;
			this._s = new Socket("localhost", 5001);
			//this._br = new BufferedReader(_s.getOutputStream());
		}catch (Exception e) {
			
		}
		
	}
}
