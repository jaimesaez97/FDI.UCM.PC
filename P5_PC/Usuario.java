import java.io.*;
import java.util.List;
import java.util.ArrayList;

class Usuario {
    
    private int id;
    private String ip;
    private List<File> list;
    
        // input: línea de 'users.txt' : PARSEAR
    public Usuario (String line){
        int i;
        String[] user;
        
        user = line.split(",");
        this.id = user.get(0);
        this.ip = user.get(0);
        this.list = new ArrayList();   
    }

}