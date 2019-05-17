package p5;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
	private ArrayList<Client> _list;
	private ServerSocket ss;
	private final int _port = 5000;
	
	public Server() {
		try {
			ss = new ServerSocket(_port);
		} catch(Exception e){
			
		}
	}
	
	public void run() {
		try {
			for( ; true ;) {
				Socket s = ss.accept();
 				OyenteServidor os = new OyenteServidor(s);
 				os.run();
			}
			
			
		} catch (Exception e) {

		}
	}
	
	public ArrayList<Client> getList(){
		return this._list;
	}
}
