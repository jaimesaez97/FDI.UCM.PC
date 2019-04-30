/*
 * 	$ java FileReaderClient.java -Dport=9999 -Dfile=FileReaderClient.java
 * 
 * */
import java.io.*;
import java.net.*;

public class FileReaderClient {
	public static void main(String[] args) {
		try {
			final int port = Integer.parseInt(System.getProperty("port"));
			final String filename = System.getProperty("file");
			System.out.println("port " + port + " file " + filename);
			
			Socket socket = new Socket("localhost", port);
			System.out.println("socket created");
			
			PrintWriter to_server = new PrintWriter(socket.getOutputStream());
			BufferedReader from_server = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("Buffers created");
			
				/* Send file */
			to_server.println(filename);
			to_server.flush();
			
			String line;			
			while((line = from_server.readLine()) != null ) 
				System.out.println(line);
			
			/* close I/O flow */
			socket.close();
			to_server.close();
			from_server.close();				
			
		} catch(Exception e) {
			System.out.println("Exception");
		}
	}
}
