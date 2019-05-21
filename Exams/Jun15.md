# Examen Programación Concurrente 
## Junio 2015

### 1. (2.5 puntos)
- El problema de la Sección Crítica se puede resolver usando un proceso coordinador y arrays, sin necesidad de instrucciones atómicas especiales. 
- Cada proceso "usuario" interacciona con el coordinador para indicarle que quiere entrar en la sección crítica y también cuando sale. 
- El coordinador "escucha" a los procesos usuario y da permiso de entrada.

#### 1.a. (1.5 puntos) Asume que hay N procesos usuario. Escribe el código que los procesos ejecutan para entrar en la sección crítica (**CSEntry**) y para salir de ella (**CSExit**), y el código del proceso coordinador. **La solución debe ser justa**. No puedes asumir ejecución atómica ni **await**.
- Voy a usar **contadores compartidos con coordinador**, que garantiza justicia.

Variables Globales

        int arrive[N] = ([N]0);
        int continue[N] = ([N]0);

CSEntry (proceso i)
    
        for[j = 1 to N]{
            in[i] =  j;
            last[j] = i;
            for[k = 1 to N with k != i]
                while(in[k] > in[i] && last[j] == i)
                    ;
        }

CSExit (proceso i)
        
            /* PROCESO i EN S.C. */
        in[i] =  0;

Coordinador


#### 1.b. (0.5 puntos) Explica cómo garantizas exclusión mutua.

#### 1.c. (0.5 puntos) Explica por qué la solución es justa.
Porque es la solución **tie-break**, que por definición es justa.

### 2. (2.5 puntos) *Hungry birds*
- Especificaciones:
    - Considera N pájaritos y un pájaro grande. 
    - Todos los pajaritos comen de un mismo plato que inicialmente tiene W gusanos.
    - Cada pajarito coge un único gusano del plato cada vez y se lo come.
    - Sólo puede haber un pajarito comiendo del plato.
    - Después de comer, el pajarito duerme y vuelve a comer, repetidamente.
    - Cuando el plato está vacío, el pajarito que comió el último gusano despierta al padre, que busca otros W gusanos más, los pone en el plato y espera a que se acabe.
    - Usando semáforos, implementa procesos pajarito y pájaro padre.
    - Declara e inicializa las variables compartidas y semáforos que sues.
- RECORDATORIO: Operaciones sobre semáforo *s*:
    - P(s) : espera s>0 y decrementa.
    - V(s) : incrementa s.

            int nr = 0, dr = 0;
            sem empty = 0, full = 1, lock = 1;
            int sharedBuffer = W;
            
            process Pajarito [i = 1 to N] 
                while(true){
                    P(lock);
                    if(nr > 0){
                        dr = dr + 1;
                        V(lock);
                        P(full);
                    }
                    
                    nr = nr + 1;
                    
                        /* PASO DE TESTIGO a readers */
                    if(dr > 0){
                        dr = dr - 1;
                        V(full);
                    } else V(lock);
                    
                    if(sharedBuffer > 0)
                        sharedBuffer = sharedBuffer - 1;
                    else{
                        /* V(lock); necesario?*/
                            /* Indico que debe SER LLENADO y me espero a que se llene */
                        V(empty);
                        P(full);
                    }
                    
                }
            
            process Pájaro 
                while(true){
                    P(lock);
                    if(sharedBuffer > 0){
                        V(lock);
                        P(empty);
                    }
                    
                    sharedBuffer = W;
                    
                    P(lock);
                    
                        /* PASO DE TESTIGO a lector/escritor */
                    if(dr > 0) {
                        dr = dr - 1;
                        V(full);
                    } else V(lock);
                }

### 3. (2.5 puntos) *Cena de los filósofos*
- Desarrolla un monitor para sincronizar las acciones de los filósofos en el problema de la *cena de los filósofos*. 
- El monitor debe tener dos operaciones:
    - **cogerTenedores(id)**
    - **soltarTenedores(id)**
- Desarrolla la implementación de las dos ops asumiento una disciplina de señalización SC *signal and continue*. 
- Debes asegurarte que dos filósofos vecinos no comen a la vez y evitar situación de bloqueo.
- Solución debe ser justa de forma que un filósofo que invoca **cogerTenedores** finalmente sea atendido.
- RECORDATORIO: Declara las variables de condición que necesites (de tipo cond) y utiliza las instrucciones *wait(cola)* para suspender al proceso en ejecución en la cola asociada a la variable de condición que recibe como parámetro, *signal(cola)* para despertar a un proceso de la cola asociada a la variable de condición que recibe como parámetro, *signal all(cola)* para despertar a todos los procesos de la cola, y *empty(cola)* para comprobar si la cola esta vacía.

		- Asumimos disciplina signal and continue.
		- Dos filosofos vecinos no comen a la vez.
		- Evitar bloqueo.
		- Solución justa.

			/* id = x € [0..N-1]*/
		monitor CenaFilosofos {
			cond espera_comer;
			int tenedor = 0;	
				/* 0 : libre; X : filósofo que lo tiene */

			procedure cogerTenedores(int id){
				while(tenedor != id)
					wait(espera_comer);	

				/* Comer */
			}

			procedure soltarTenedores(id){
				tenedor = (id + 1) % N;
				signal_all(espera_comer);
			}
		}
        


### 4. (2.5 puntos)
- Desarrolla en Java la implementación de un servidor **EchoServer** concurrente usando **Sockets** que funcione de la siguiente forma: cada vez que el servidor recibe nueva solicitud de conexión crea un nuevo proceso para atenderla.
- El nuevo proceso establece los flujos de entrada y salida para la comunicación entre cliente y servidor y comienza a leer repetidamente una línea por flujo de entrada y la envía tal cual por flujo de salida.
- El proceso termina cuando llega la línea "Bye".
- El puerto en el que se espera conexión es el 10008.
- RECORDATORIO: En la implementación utiliza las siguientes clases y métodos de las librerías de Java: clase ServerSocket (métodos accept y close), clase Socket (métodos getInputStream, getOutputStream y close), clase PrintWriter (método println), clase BufferedReader (método readLine).

        import java.net.*;
        import java.io.*;
        
        class EchoServer {
            private ServerSocket ss = new ServerSocket(10008);
            private Socket s;
			
			public class Request extends Thread {
			
				private PrintWriter pw;
				private BufferedReader br;
				
				public Request(Socket s){
					pw = new PrintWriter(s.getOutputStream());
					br = new BufferedReader(s.getInputStream());
				}
				
				public void run(){
					String str = br.readLine();
					while(!str.equals("Bye")){
						pw.println(str);
						str = br.readLine();
					}
				}
			}
			
			public void run(){
				while(true){
					Socket s = new Socket(ss.accept());
					Request th = new Request(s);
					s.start();
				}
			}
			
        }