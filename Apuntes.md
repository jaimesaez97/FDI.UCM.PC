# Programación Concurrente
## Algoritmos vistos

### 1. Problema de la Sección Crítica

		process CS[i = 1 to N]
			while(true){
				CSEntry;
				CS;
				CSExit;
				noCS;
			} 

#### 1.1. Solución 1 : Test & Set

- Para esta solución asumimos una operación **atómica** Test&Set, que cambia el valor del booleano que recibe por referencia y devuelver el que le entra.

		bool TS(bool & lock){
			<bool initial = lock;
			lock = true;
			return initial;> 
		}

		bool lock = false;
		process CS[i = 1 to N]{
			/*CSEntry*/
			while(true){
				while lock			/* 1ª comp: el primero que llega sale y le resto se bloquean par no ejecutar T&S (+ cara)	*/
					;	
				while(TS(lock))		/* 2ª comp: el primero adquiere el cerrojo y pone lock a false, saltándose el 2º while		*/
					while lock		/* 3ª comp: comprobación BARATA para los procesos que se bloquena aquí (en vez de T&S)		*/
						;
			}
			/*CS*/

			/*CSExit*/
			lock = false;
			/*noCS*/
		}

#### 1.2. Solución 2 : Espera activa (Spin Locks)

- Para esta solución representamos una operación <await B; SC;> basándonos en el comportamiento de un SpinLock.

		CSEntry;
		while(!B){
			CSExit;
			Delay;
			CSEntry;
		}
		CS;
		CSExit;
		noCS;

#### 1.3. Solución 3: Algoritmo Tie Break

- En este algoritmo hay que entender los índices:
	- i : cada uno de los procesos que ejecutan CS
	- j : cada una de las etapas por las que pasa cada proceso
	- k : cada uno de los procesos (menos i) con los que me comparo:
		- in[k] > in[i] : "que no haya otro proceso k que esté en una etapa por delante"  
		- last[j] == i  : "que yo sea el último proceso en la etapa j"
- De esta forma, el algoritmo espera cuando:
	- "Hay un proceso que va por una etapa por delante o el último de la etapa j soy yo"

		process CS[i = 1 to N]
			while(true){
					/* Para cada una de las etapas*/
				for[j = 1 to N]{
					in[i] = j;		/* Proceso i llega a etapa j */
					last[j] = i;
					for[k = 1 to N with k != i]
						while(in[k] >= in[i] && last[j] == i)
							;
				}
				CS;
					/* Proceso i en SC */
				in[i] = 0;
				noCS;
			}
	
#### 1.4. Solución 4: Algoritmo del Ticket

- Para esta solución suponemos operación atómica Fetch & Add, parecido a T&S pero con un incremento de la variable:

		int FA(int & var, int & incr) = 
		< 	int tmp = var;
			var = var + incr;
			return tmp;
		>

		int turn[1..N] = ([N]0);
		int next = 1;
		int number = 1;

		process CS[i = 1 to N]
			while(true){
				turn[i] = FA(number, 1);
				while(turn[i] != next)
					;
				CS;
				next = next + 1;
				noCS;
			}

- Para implementar una solución a bajo nivel de este algoritmo habría que sustutuir F&A por ejecución atómica del incremento.

#### 1.5. Solución 5: Algoritmo Bakery

- Para esta solución hay que suponer una opración no atómica (fácil de implementar) que deja entrar solo 1 proceso cuando 2 tienen igual turno:

		(a,b) >> (c,d) = (a > c) or (a == c && b > d)


		int turn[1..N] = ([N]0);

		process CS[i = 1 to N]{
			turn[i] = 1;
			turn[i] = max(turn) + 1;

			for[j = 1 to N with j != i]
				while((turn[j] != 0) && 
					  (turn[i],i) >> (turn[j],j))
					;
			CS;
			turn[i] = 0;
			noCS;
		}

### 2. Contadores Compartidos

#### 2.1. Solución 1: Con coordinador

- Proceso **worker**:

		int arrive[1..N] = ([N]0);
		int continue[1..N] = ([N]0);

		process Worker[i = 1 to N]
			while(true){
				arrive[i] = 1;			/* Aviso de que he llegado */
				while(continue[i] == 0)	/* Espero a que COORD me diga de continuar*/
					;

				/* ... */

				continue[i] = 0;
			}

- Proceso **coordinador**:

		process Coord
			while(true){
				for[j = 1 to N ]
					while(arrive[i] == 0)	/* Espero que lleguen todos */
						;
				for[j = 1 to N]
					continue[i] = 1;		/* Aviso que ya están todos listos */
			}

#### 2.2. Solución 2: Sin coordinador (en árbol)

- Proceso HOJA L

		arrive[L] = 1;
		while(continue[L] == 0)
			;
		...
		continue[L] = 0;
		noCS;

- Proceso INTER I

		while(arrive[left(I)] == 0)
			;
		arrive[left(I)] = 0;
		while(arrive[right(I)] == 0)
			;
		arrive[right(I)] = 0;
		arrive[I] = 1;
		while(continue[I] == 0)
			;
		/* ... */
		continue[I] = 0;
		continue[left(I)] = 1;
		continue[right(I)] = 1;

- Proceso ROOT R

		while(arrive[left(R)] == 0)
			;
		arrive[left(R)] = 0;
		while(arrive[right(R)] == 0)
			;
		arrive[right(R)] = 0;
		/*...*/
		continue[left(R)] = 1;
		continue[right(R)] = 1;

### 3. Barreras simétricas

#### 3.1. Solución 1: Entre dos procesos

- Proceso I
		
		<await arrive[i] == 0; >
		arrive[i] = 1;
		<await arrive[j] == 1; >
		arrive[j] = 0;

- Proceso J

		<await arrive[j] == 0; >
		arrive[j] = 1;
		<await arrive[i] == 1; >
		arrive[i] = 0;

#### 3.2. Solución 2: Barrera mariposa 

- El número de procesos debe ser potencia de dos.
- De esta forma cada proceso se va comunicando con una distancia de 2^s (s número de etapa) a la derecha para crear una cola única de procesos en espera.

#### 3.3. Solución 3: Barrera diseminación

- BARRERA(I):

		process Worker[i = 1 to N]
			for[s = 1 to numEtapas]{
					1) MARCAR arrive[i]
				arrive[i] = arrive[i] + 1;
					2) DETERMINAR a qué j espera i (mariposa = 2^s)
				// depende de la barrera usada
					3) ESPERAR a j
				while(arrive[j] < arrive[i])
					;
			}

#### Ej: 

		sum[0] = a[0];
		for[i = 1 to N-1]
			sum[i] = sum[i-1] + a[i];

- Implementación:

		process sum[i = 0 to N-1]{
			int d = 1;		/* Distancia al proceso que nos comunicamos */
			sum[i] = a[i];
			while(d < n){
				old[i] = sum[i];
				BARRERA(F);
				if(i - d >= 0)	/* Si existe algún elemento d puestos a la DER de i */
					sum[i] = old[i-d] + sum[i];
				BARRERA(I);
				d = d + d;	/* Potencias de 2 */
			}
		}

### 4. Semáforos

#### 4.1. Semáforos como MÚTEX

	sem mtx = 1;
	process CS[i = 1 to N]
		while(true){
			P(mtx);	/* down() */
			CS;
			V(mtx);	/* up() */
			noCS;
		}

#### 4.2. Semáforos como BARRERA

		sem[n] mtx = ([n]0);
		process Worker[i = 1 to N]
			while(true){
				/*iteración-i*/
				for[j = 1 to N with j != i]{
					V(sem[i]);
					P(sem[j]);
				}
			}

#### 4.3. Problema Productor Consumidor con Semáforos

##### 4.1. 1 productor, 1 consumidor 1 buffer tam 1

		int buf;
		sem empty = 1;
		sem full = 0;
		process Prod
			while(true){
				P(empty);
				buf = data();
				V(full);
			}

		process Cons
			while(true){
				P(full);
				consumeBuf(buf);
				V(empty);
			}

##### 4.2. m productores, n consumidores, 1 buffer tam 1

- Esta solución es EXACTAMENTE IGUAL a la anterior ya que el buffer tiene tamaño uno, y por ello no pueden leer/escribir a la vez.

##### 4.3. 1 productor, 1 consumidor, 1 buffer tam n

		sem empty = N;
		sem full = 0;
		int buf[N] = ([N]0);
		int fin = 0;		/* Índice de producción */
		int ini = 0;		/* Índice de consumición */
		
		process Productor
			while(true){
				P(empty);
				buf[fin] = dato;
				fin = (fin + 1) % N;
				V(full);
			}
		process Consumidor
			while(true){
				P(full);
				result = buf[ini];
				ini = (ini + 1) % N;
				V(empty);
			}

##### 4.4. m productores, k consumidores, 1 buffer tam N

		sem mutx_prod = 1;	/* Mútex de producción */
		sem mutx_cons = 1;	/* Mútex de consumición */
		sem empty = N;		
		sem full = 0;
		int fin = 0;		/* Índice de producción */
		int ini = 0;		/* Índice de consumición */

		process Productor[i = 1 to N]
			while(true){
				P(empty);
				P(mtx_prod);

				buf[fin] = dato();
				fin = (fin + 1) % N;

				V(mtx_prod);
				V(full);
			}

		process Consumidor[i = 1 to N]
			while(true){
				P(full);
				P(mtx_cons);

				result = buf[ini];
				ini = (ini + 1) % N;

				V(mtx_cons);
				V(empty);
			}

### 5. Cena de los filósofos

#### 5.1. Con un Semáforo Contador de Recursos

 - Primero coge tenedor IZQ, luego DER y luego come.


		sem fork[N] = ([N]1);

		process filosofo[i = 0 to N-2]
			while(true){
				P(fork[i]);
				P(fork[(i + 1)%N]);
					/* COMER */
				V(fork[i]);
				V(fork[(i+1)%N]);
					/* PENSAR */

		process ult_filosofo{
			P(fork[0]);
			P(fork[n-1]);
				/* COMER */
			V(fork[0]);
			V(fork[N-1]);
				/* PENSAR */
		}

#### 5.2. Con una cola de condición

### 6. Paso de testigo
#### 6.1. Problema Lectores/Escritores
##### 6.1.1. Solución con AWAIT

		int nw = 0, nr = 0;
		process Reader[i = 1 to M]
			while(true){
				< await (nw == 0); nr = nr + 1; >
				read(DB);
				<nr = nr - 1; >
			}

		process Writer[i = 1 to N]
		while(true){
			< await (nr == 0 ^ nw == 0); nw = nw + 1; >
			write(DB);
			< nw = nw - 1; >
		}

##### 6.1.2. Solución con SEMÁFOROS

- La ejecución atómica la implementamos asegurando exclusión mutua en nr, nw.

		int nw = 0, nr = 0;
		sem rw = 1, r_mtx = 1, wr_mtx = 1;

		process Reader[i = 1 to N]
			while(true){

			P(r_mtx);
			nr = nr + 1;
			if(nr > 0)
				P(rw);
			V(r_mtx);

			read(DB);

			P(r_mtx);
			nr = nr - 1;
			if(nr = 0)
				V(rw);
			V(r_mtx);
			}

		process Writer[i = 1 to M]
			while(true){
				P(wr_mtx);
				nw = nw + 1;
				if(nw > 0)
					P(rw);
				V(wr_mtx);

				write(DB);

				P(wr_mtx);
				nr = nr - 1;
				if(nr == 0)
					V(rw);
				V(wr_mtx);

			}

##### 6.1.3. Solución 1 con PASO DE TESTIGOS

		int nr = 0, dr = 0, nw = 0, dw = 0;
		sem r = 0, w = 0, mtx = 1;

		process Reader[i = 1 to M]
			while(true){
				P(mtx);
				if(nw > 0){
					dr = dr + 1;
					P(r);			/* Paso de Testigo a lector en espera */
				}
				nr = nr + 1;

				if(dr > 0){
					dr = dr - 1;	/* Recibo de Testigo de lector en espera */
					V(r);
				} else V(mtx);

				read(DB);

				P(mtx);
				nr = nr - 1;
				if(nr == 0 ^ dw > 0){
					dw = dw - 1;
					V(w);			/* Paso de Testigo a escritoe en espera */
				}

			}

		process Writer[i = 1 to N]
			while(true){
				P(mtx);
				if(nr > 0){
					dw = dw + 1;
					P(w);		/* Paso de Testigo a escritor en espera */
				}
				nw = nw + 1;
				if(dw > 0){
					dw = dw - 1;
					V(w);		/* Recibo de Testigo de escritor en espera */
				} else V(mtx);

				write(DB);

				P(mtx);
				nw = nw - 1;
				if(dw > 0){
					dw = dw - 1;
					V(w);		/* Paso de Testigo a escritor en espera */
				} else if(dr > 0){
					dr = dr - 1;
					V(r);		/* Paso de Testigo a lector en espera */
				} else V(mtx);
			}

### 7. Monitores

#### 7.1. Implementación semáforo convencional

		monitor Semaforo{
			int s = 0;
			cond pos;

			procedure V(){
				s = s + 1;
				signal(pos);
			}

			procedure P(){
				if(s == 0)
					wait(pos);
				s = s + 1;
			}
		}

##### 7.2. Implementación Semáforo FIFO

		monitor SemFifo{
			int s = 0;
			cond pos;

			procedure V(){
				if(empty(pos))
					s = s + 1;
				ele signal(pos);
			}

			procedure P(){
				if(s == 0)
					wait(pos);
				else s = s + 1;
			}
		}

#### 7.3. Implementación Bounde Buffer

		montiro BB {
			typeBuff buf[N];		
			int ini = 0;			/* 1er entero a consumir */
			int fin = 0;			/* 1ª pos libre para prod */
			int count = 0;			/* nº elementos (fin - ini) */
			cond full, empty;		/* colas de condicion prod,cons */

			procedure produce(typeProd p){
				while(count == N)
					wait(full);

				buf[fin] = p;
				fin = (fin + 1) % N;
				count = count + 1;
				signal(empty);
			}

			procedure consume(typeProd p){
				while(count == 0)
					wait(empty);

					p = buf[ini];
					ini = (ini + 1) % N;
					count = count - 1;
					signal(full);
			}
		}

#### 7.4. Implementación Contador Accesos

		monitor RWController {
			int nr = 0, nw = 0;
			cond read, write;

			procedure readRequest(){
				while(nw > 0) wait(read);
				nr = nr + 1;
			}

			procedure readRelease(){
				nr = nr - 1;
				while(nr == 0 ^ nw > 0) signal(write);
			}

			procedure writeRequest(){
				while(nr > 0 || nw > 0) wait(write);
				nw = nw + 1;
			}

			procedure writeRelease(){
				nw = nw - 1;
				while(nw == 0) signal(read);		/* Estrategia de RELEVO: vacío todos los lectores antes de un nuevo escritor */
													/* Otra opción sería   signal(write);       y que se peleen ellos */
																		   signal_all(read);
			}	

		}

#### 7.5. SJN con monitores

		monitor SJN {
			bool free = true;
			cond turn;

			procedure request(int time){
				if(free) free = false;
				else wait(turn, time);
			}

			procedure release(){
				if(empty(turn)) free = true;			
				else signal(turn);
			}
		}

#### 7.6. Implementaciones TIMER
##### 7.6.1. v1: ineficiente al despertar a todos

		monitor Timer {
			int t = 0;
			cond check;

			procedure delay(int time){
				int makeTime = t + interval;
				while(makeTime > t) waitt(check);
			}

			procedure tick(){
				t = t + 1;
				signalAll(check);
			}
		}

##### 7.6.2. v2: cola de prioridad

		monitor Timer {
			int t = 0;
			cond check;

			procedure delay(int interval){
				int makeTime = t + interval;
				while(makeTime > t) wait(check, makeTime);
			}

			procedure tick(){
				t = t + 1;
				while(!empty(check) &&
					   mirank(check) < t)
					signal(check);
			}
		}

#### 7.7. Comunicación Cliente Servidor 
##### 7.7.1. Ejemplo Barbería
Monitor:

		monitor Barberia {
			cond b_available, c_ocup, open_door, cust_out;
			int barbero = 0, sillas = 0, abierto = 0;

			procedure getHairCut(){
				while(barbero == 0) wait(b_available);
				barbero = barbero - 1;

				sillas = sillas + 1; 
				signal(c_ocup);

				while(abierto == 0) wait(open_door);

				abierto = abierto - 1;
				signal(cust_out);
			}

			procedure getNextCustomer(){
				barbero = barbero + 1;
				signal(b_available);
				while(sillas == 0) wait(c_ocup);
				sillas = sillas - 1;
			}

			procedure finishedCut(){
				abierto = abierto + 1;
				signal(open_door);
				while(abierto == 0) wait(cust_out);
			}
		}

Cliente: 
			
			barberia.getHairCut();

Barbero:
			
			while(true){
				barberia.getNextCustomer();
				/*CortarPelo*/
				barberia.finishedCut();
				/*RestTime*/
			}

### 8. Implementaciones del kernel
#### 8.1. Semáforos del kernel

		int executing = 0;
		cond blocked, ready;

		procedure createSem(int init, int * name){
			obtener descriptor nuevo name;
			value = init;
			dispatcher();
		}

		procedure P(int name){
			cargar descriptor de sem name;
			if(value > 0) value = value - 1;
			else{
				insertar executing en blocked;
				executing = 0;
			}
			dispatcher();
		}

		procedure V(int name){
			cargar descriptor de sem name;
			if(blocked is empty)
				value = value + 1;
			else{
				sacar al proceso id de blocked;
				insertar id en ready;
			}				
			dispatcher();
		}
		
#### 8.2. Monitores

		procedure createMon() : creo, cogo value y devuelvo

		procedure enter(int mName){
			encontrar descriptor monitor mName;
				/* Si el procesador esta ocupado lo libero */
			if(mLock == 1){
				insertar executing en cola(mName);
				executing = 0;
			} else mLock = 1;
			dispatcher();
		}

		procedure exit(int nMame){
			encontrar descriptor monitor mName;
			if(cola(mName) not empty){
				extraer id de la cola(mName);
				insertar id en ready;
			} else mLock = 0;
			dispatcher();
		}

		procedure wait(int mName, int cName){
			encontrar descriptor monitor mName;
			insertar executing en cola(cName);
			executing = 0;
			exit(mName);
		}

		procedure signal(int mName, int cName){
			encontrar descriptor monitor mName;
			if(cola(mName) not empty){
				extraer id de la cola cName;
				insertar id en la cola(mName);
			}			
			dispatcher();
		}

### 9. Paso de mensajes (programación distribuida)

#### 9.1. Programa filtro

		chan input(char);
		chan output(char[]);

		process FIltro
			while(true){
				char line[MAX];
				int i = 0;

				receive input(line[i]);
				while(line[i] != RET && i < MAX){
					i = i + 1;
					receive input(line[i]);
				}
				send output(line);
			}

#### 9.2. Programa Merge

- Existen dos flujos de entrada y hay que sacar ordenados ambos por uno de salida.

		chan in1(int);
		chan in2(int);
		chan out(int);

		process merge{
			int v1, v2;
			receive in1(v1); receive in2(v2);
			while(v1 != EOS && v2 != EOS){
				if(v1 <= v2){
					send out(v1);
					receive in(v1);
				} else {
					send out(v2);
					receive in2(v2);
				}
			}
			if(v1 == EOS){
				while(v2 != EOS){
					send out(v2);
					receive in2(v2);
				}
			} else{
				while(v1 != EOS){
					send out(v1);
					receive in1(v1);
				}
			}
			send out(EOS);
		}

#### 9.3. Implementación CLiente/Servidor

		chan request(int id, paramsType params, typeOp op);
		chan reply[N](resultType ret);

		process Server{
			int clientId;
			paramsType params;
			resultType ret;

			while(true){
				receive request(clientId, params, op);
				/*ejecutar cuerpo op, params*/
				send reply[clientId](ret);
			}
		}

		process Client[i = 0 to N-1]{
			typeParams params;
			typeOp op;
			typeResult ret;

			send request(i, params, op):
			receive reply[i](ret);
		}

#### 9.4. Implementación monitor asignador de recursos con cola de espera

		montir Recursos{
			int avail = MAX;
			set units = init();
			cond free;

			procedure acquire(int & id){
				if(avail == 0) wait(free);
				else avail = avail - 1;
				take(units, id);
			}

			procedure release(int id){
				insert(units,id);
				if(empty(free)) avail = avail + 1;
				else signal(free);
			}
		}

#### 9.5. Implementacion monitor N servidores, M clientes

		chan open(int clientId, String name);
		chan access[N](int op, typeArg args);
		chan openReply[M](int serverId);
		chan accessReply[M](String info);

		process Client[j = 0 to M-1]{
			int serverId;
			String ret;
			typeArg args;

			send open(j, "a.txt");

			receive openReply[j](serverId);

			send access[serverId](READ, args);

			receibe accessReply[j](ret);
		}

		process Server[i = 0 to N-1]{
			int clientId;
			String fileName;
			typeOp op;
			typeArg args;
			String ret;

			while(true){
				receive open(clientId, fileName);
				bool ok = fopen(fileName);

				if(ok){
					send openReply[clientId](i);

					receive access[i](op, args);

					switch(op){
						case READ:	/*...*/

						case WRITE: /*...*/

						case CLOSE: /*...*/
					}
					send accessReply[clientId](ret);
				}
#### 9.6. Implementación N procedimientos, V enteros

- Estas tres implementaciones implementan un entorno en el que a cada proceso le llega un número entero y tienen que saber TODOS cuál es el máximo y el mínimo de los N números (N procesos) que les hanm transmitido.

##### 9.6.1. Arquitectura centralizada

	chan valores(int);
	chan result[N](int min, int max);

	process Peer[k = 0]{
		int v = init();
		int new, min = v, max = v;

		for[i = 1 to N-1]{
			receive valores(new);
			if(new < min) min = new;
			if(new > max) max = new;
		}
		for[i = 1 to N-1]
			send results[i](min,max);	/* BROADCAST */
	}

##### 9.6.1. Arquitectura simétrica

	chan valores[N](int valor);

	process Peer[i = 0 to N-1]{
		int v = init();
		int new, min = v, max = v;

		for[j = 0 to N-1 with j != i]
			send valores[j](v);			/* BROADCAST */

		for[j = 0 to N-1]{
			receive valores[i](new);
			if(new < min) min = new;
			if(new > max) max = new;
		}
	}

##### 9.6.1. Arquitectura en anillo

		chan valores[N](int min, int max);

		process Peer[i = 0]{
			int v = init(), min = v, max = v;

			send valores[1](min,max);

			receive valores[0](min,max);

			send valores[1](min,max);
		}

		process Peer[i = 1 to N-1]{
			int v = init(), min, max;

			receive valores[i](min, max);

			if(min > v) min = v;
			if(max < v) max ) v;

			send valores[i+1](min,max);
			receive valores[i](min,max);
			if(i != N-1) send valores[i](min,max);
		}