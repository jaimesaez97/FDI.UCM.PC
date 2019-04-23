import java.lang.Thread;
import java.net.ServerSocket;

class Emisor extends Thread {
    
    //private int ip;
    private int port;
    private FlujoSalida fout;
    
    public Emisor(/*int ip,*/ int p){
        super();
        //this.ip   = ip;
        this.port = p;
    }
    
    public void run (){
            // crear ServerSocket (IP:Port)
        ServerSocket ss = new ServerSocket(this.port);
        
            // accept
        ss.accept();
        
            // crear FlujoSalida
        
        
            // escribo Fichero
        
        
            // cerrar
    }
}