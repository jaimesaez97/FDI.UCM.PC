# Examen Programación Concurrente 
## Junio 2014

### 1. (1,5 puntos) Considera el siguiente fragmento de código

	int u = 0, v = 1, w = 2, x;
	CO x = u + v + w;
	// u = 3;
	// v = 4;
	// w = 5;
	OC

- Asumir que las instrucciones atómicas son la lectura y escritura de variables.

#### 1.1. ¿Qué valores finales puede tomar la variable x si `x = u + v + w` se evalúa de izquierda a derecha?

- Es fácil darse cuenta que las variables u, v y w pueden tomar cualquier valor (u € [0,3], v € [1,4], w € [2,5]) ya que en cualquier momento el planificador puede darles la entrada en el procesador.

- Por lo tanto, x puede tomar cualquier combinación de las tres:

		u		v		w		x
		0		1		2	=	3
		0		1		5	=	6
		0		4		2	=	6
		0		4		5	=	9
		3		1		2	=	6
		3		1		5	=	9
		3		4		2	=	9
		3		4		5	=	12
		
- x € [3, 6, 9, 12]

#### 1.2. ¿Qué valores finales puede tomar la x asumiendo que la expresión `x = u+v+w` se evalúa en cualquier orden?

- Como hemos argumentado antes, u, v y w pueden tomar cualquier combinación de valores, luego no depende del orden de evaluación de la expresión, x € [3, 6, 9, 12].
			

### 2. (2,5 puntos)


Variables y semáforos
		
		int dr = 0, buffer = 0;
		sem mtx = 1, read = 0;
		
boradcast(int m)
		
		P(mtx);
		
		buffer = m;
		
		if(dr > 0){
			dr = dr - 1;
			V(read);		/* Paso de Testigo a lector bloqueado */
		} else V(mtx);
			
listen(int x)

		P(mtx);
		dr = dr + 1;
		V(mtx);
		P(read);	/* Recibo de Testigo de lector bloqueado */
		
		x = buffer;
		
		if(dr > 0){
			dr = dr - 1;
			V(read);		/* Paso de Tesgigo a lector bloqueado */
		} else V(mtx);	


### 3. (2,5 puntos) 

	monitor PrintAllocator {
		boolean impA = true, impB = true;		/* flag de si impX está ocupado */
		cond wait;								/* cola de espera */
		
		// tipoProc € [0:A, 1:B, 2:C]
		procedure int request(int tipoProc){
			while((tipoProc == A && !impA) 	||
				  (tipoProc == B && !impB)	||
				  (tipoProc == C && !impA && !impB))
				wait(wait);
			
			switch(tipoProc){
				case A:
					impA = false;
					return tipoA;
				case B:
					impB = false;
					return tipoB;
				case C:
					if(impA) {
						impB = false;	/* Como técnica preferimos antes la A */
						return tipoB;
					}
					else {
						impA = false;
						return tipoA;
					}
			}
		}
		
		// tipoImp € [1:A, 2:B]
		procedure release(int tipoImp){
			switch(tipoImp){
				case A:
					impA = true;
					break;
				case B:
					impB = false;
					break;
			}
			signalAll(wait);
		}
	}

### 4. (2,5 puntos)

- Clase `EmisorFichero`.
- Usa `Sockets`.
- Cada vez que se recibe solicitud de emisión de fichero:
	1. Establece los flujos de entrada y salida.
	2. Recibe por flujo entrada un String con nombre de fichero.
	3. Envía por flujo de salida String "ok"+(nombre fichero).
- Puerto servidor 100.

	
		class EmisorFichero {
			
			ServerSocket ss = new ServerSocket("localhost", 100);
			Socket s;
			
			public void run(){
				while(true){
					s = new Socket(ss.accept());
					Emitir th = new Emitir(s);
					th.start();
				}
			}
			
			public class Emitir extends Thread {
				
				BufferedReader br;
				PrintWriter pw;
				
				public Emitir(Socket s){
					br = new BufferedReader(new InputStream(s.getInputStream()));
					pw = new PrintWriter(new OutputStream(s.getOutputStream()));
				}
				
				public void run(){
					String filePath;
					
					filePath = br.readLine();
					pw.println("ok" + filePath);
					
					br.close();
					pw.close(),
				}
			}
			
		}
	