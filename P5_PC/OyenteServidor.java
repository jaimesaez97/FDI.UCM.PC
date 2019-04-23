/*
    - ¿ Cómo se vuelca la información desde aquí al usuario?
    - ¿ Cómo llega el flujo de entrafa FIN y salida FOUT aquí?
*/

import java.lang.Thread;

class OyenteServidor extends Thread {
        
    private FlujoEntrada fin;   // El flujo de ENTRADA/SALIDA (fin,fout) se tiene que pasar por parámetro
    private FlujoSalida fout;
    private int port;           // puerto a crear ServerSocket (++)
    
    public OyenteServidor () {
        super();
    }
    
    public int getPort () {
        return this.port;
    }
    
    public void run(){
        while(true){    // proceso siempre en ejecución
            Mensaje m = (Mensaje)this.fin.readObject();
            
            switch (m.getType()) {
                case TypeMSG.CONNECTION_CONFIRMATION_MESSAGE:
                        // avisar al usuario (o nada)
                    System.out.println("Connection Confirmated");
                    break;
                case TypeMSG.USER_LIST_CONFIRMATION_MESSAGE:
                        // volcado de INFO al usuario
                    m.getMsg();
                    break;
                case TypeMSG.FILE_REQUEST_MESSAGE:
                        // crear proceso Emisor
                    Emisor p = new Emisor(this.port);
                    // p.start() ¿?
                    this.port++;                        // puerto SS acumulativo
                    fout.sentObject(READY_CS_MESSAGE);  // IP:Port SS
                    break;
                case TypeMSG.READY_SC_MESSAGE:
                    Receptor p = new Receptor();
                    // p.start() ¿?
                    break;
            }
        }
    }
}