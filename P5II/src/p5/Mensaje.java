package p5;
import java.util.ArrayList;


public interface Mensaje {

	public enum TypeMSG {
		HELP, LOGIN, LOGINOK, LOGOUT,
		LOGOUTOK, LIST, LSTOK, REQFILE,
		REQFILEOK, READYCS, READYSC
	};
	
	public final int LOGIN = 0;
	public final int LIST = 1;
	public final int DOWNLOAD = 2;
	public final int LOGOUT = 3;
	////////////////////////////////
	public final int LOGIN_ACK = 4;
	public final int LIST_ACK = 5;
	public final int DOWNLOAD_ACK = 6;
	public final int LOGOUT_ACK = 7;
	
	
	public class Header implements Mensaje {
		public String src;
		public String dst;
		public int type;
		
		public Header(String s, String d, int t) {
			this.src = s;
			this.dst = d;
			this.type = t;
		}
		
		public int getType() {
			return this.type;
		}
	}	
	
	public class ACK extends Header{

		public ACK(String s, String d, int t) {
			super(s, d, t);

		}
		
	}
	
	public class Login extends Header{

		public Login(String s, String d, int t) {
			super(s, d, t);

		}
		
	}
	
	public class Logout extends Header{

		public Logout(String s, String d, int t) {
			super(s, d, t);
		}
		
	}
	
	public class List extends Header{
		public List(String s, String d, int t) {
			super(s, d, t);

		}

		public ArrayList<Usuario> _users;
	}
	
	public class Help extends Header{
		public String _help;
		
		public Help(String s, String d, int t) {
			super(s, d, t);
		}

	}
}
