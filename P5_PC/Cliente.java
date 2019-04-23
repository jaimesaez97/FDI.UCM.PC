import java.net.*;
import java.util.Scanner;
import java.io.*;

class Cliente {
    
    private String userName;
    private String ip;
    private Socket s;
    // private FlujoEntrada fin;
    // private FlujoSalida fout;
    
    public Cliente (String name, String ip) {
        super();
        this.userName = name;
        this.ip =  ip;
    }
    
    public static void main(String[] args){
        Scanner in;
        int port;           // nº puerto acumulativo
            
        in = new Scanner(System.in);
        
            // al margen de la voluntad del usuario, Cliente puede actuar como emisor de archivos
            // como propietario. Esto se lleva a cabo en un segundo plano
        OyenteServidor os = new OyenteServidor();
        os.start();
        
            // se pide al usuario su nombre
        System.out.println("Introduzca el nombre de usuario: ");
        this.userName = in.next();
        
        
        
        
        switch(printMenu()){
            case 1:
                getUserList();
                break;
            case 2:
                loadFile();
                break;
            default:
                break;
        }
    }
    
    public void getUserList () {
        
    }
    
    public void loadFile () {
        
    }
    
    public int printMenu(Scanner in){
        int opt;
        
        System.out.println("            IMPRESIÓN DEL MENÚ");
        System.out.println("1.- Conocer Nombre de Todos Usuarios Conectados");
        System.out.println("            2.- Descargar ficheros");
        opt = in.next();
        
        return opt;
    }
}