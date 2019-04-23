/*
    - ¿Cómo llega flujo entrad FIN salida FOUT aquí?
    - 
*/

import java.lang.Thread;

class OyenteCliente extends Thread {
    
    private Connection c;
    
    
    public OyenteCliente(){
            // crear conexiones
    }
    
    public void run(){
        
        while(true){ // proceso siempre en ejecución
            Mensaje m = (Mensaje)fin1.readObject();
            switch(m.getType()){
                case TypeMSG.CONNECTION_MESSAGE:
                        // mandar confirmación por fin1
                    fout1.sentObject(CONNECTION_CONFIRMATION_MESSAGE);
                    break;
                case TypeMSG.USER_LIST_MESSAGE:
                        // mandar lista usuarios
                    fout1.sentObject(USER_LIST_CONFRIMATION_MESSAGE);
                    break;
                case TypeMSG.FILE_REQUEST_MESSAGE:
                    fout2.sentObject(FILE_REQUEST_MESSAGE);
                    break;
                case TypeMSG.READY_CS_MESSAGE:
                    fout1.sentObject(READY_SC_MESSAGE);
                    break;
                case TypeMSG.CLOSE_CONNECTION_MESSAGE:
                    fout1.sentObject(CONNECTION_CLOSED);
                    break;
            }
        }
    }
}