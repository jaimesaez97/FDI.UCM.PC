package p5;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import p5.Mensaje.Header;
import p5.Mensaje.List;



public class OyenteServidor extends Thread {

	private InputStream _is;
	private OutputStream _os;
	private final String help = "help.     this command\n"
							  + "login.    login to system\n"
							  + "logout.   logout from system\n"
							  + "list.	   get user list"
							  + "close.	   close connection\n";	
	
	public OyenteServidor(Socket s) {
		try {
			_is = s.getInputStream();
			_os = s.getOutputStream();
		} catch(Exception e) {
			
		}
	}
	
	public void run() {
		while(true) {
			try {
				StringWriter sw = new StringWriter();
				BufferedReader br = new BufferedReader(new InputStreamReader(_is));
				ObjectInputStream sIn = new ObjectInputStream(_is);
						
				Object msg = sIn.readObject();
				Header head = (Header) msg;
				switch(head.getType()) {
				
				case 0:	// LOGIN
					break;
				case 1: // LIST
					break;
				case 2:	// DOWNLOAD
					break;
				case 3: //LOGOUT
					break;
				case 4: // LOGIN_ACK
					System.out.println("Connection succesfull");
					break;
				case 5:	// LIST_ACK : suponemos cliente no recibe mensaje logout
					List l = (List) msg;
					ArrayList<Usuario> users = new ArrayList<Usuario>(l._users);
					printList(users);
					break;
				case 6: // DOWNLOAD_ACK
					//Terminar
						/* crear proceso receptor para recibir file */
					break;
				case 7:	// LOGOUT_ACK : suponemos cliente no recibe mensaje list
					System.out.println("Connection aborted succesfully");
					break;
				
					
				}
				
			} catch(Exception e) {
				
			}
		}
	}

	private void printList(ArrayList<Usuario> users) {
		int i = 0;
		
		for( ; i < users.size(); ++i) {
			
			
		}
	}
}
