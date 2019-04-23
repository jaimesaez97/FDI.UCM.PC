import java.util.List;
import java.util.Arraylist;
import java.io.*;


class Servidor {
    
    private List<Usuario> userList;
    private List<OyenteCliente> listeners;
    
    public Servidor () {
        this.userList = new ArrayList();
        this.listeners = new ArrayList();
    }
    
    public static void main (String[] args) {
        int i;
        
            // lee de un fichero 'users.txt' la información de los usuarios registrados
            // y la almacena en this.userList
        getInfoFromFile();
        
            // atiende de forma concurrente todas las peticiones que realizan los clientes
            // conectados al sistema
        i = 0;
        while(i < this.listeners.length) {
            this.listeners.get(i).start();
        }
    }
    
    public void getInfoFromFile(){
        FileReader fr;
        BufferedReader br;
        String i;
        Usuario u;
        OyenteCliente oc;
        
        fr = new FileReader("c:/P5_PC/users.txt");
        br = new BufferedReader(fr);
        
        while((i=br.readLine()) != null){
            u = new Usuario(i);
            this.userList.add(u);
            
            oc = new OyenteCliente();
            this.listeners.add()
        }
        
    }
}