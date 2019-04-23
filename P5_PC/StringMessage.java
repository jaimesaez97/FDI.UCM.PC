public class StringMessage extends Message {
    
    public String msg;
    
    public StringMessage(TypeMSG t, String s, String d, String m){
        super();
        this.type = t;
        this.src  = s;
        this.dst  = d;
        this.msg  = m;
    }
    
    public TypeMSG getTipo(){
        return this.type;
    }
    
    public String getOrigen(){
        return this.src;
    }
    
    public String getDestino(){
        return this.dst;
    }
    
    public String getMsg() {
        return this.msg;
    }
}