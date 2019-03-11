# Programación Concurrente
## Tema 2 : Procesos y sincronización

### Introducción
Los programas concurrentes ependen de componentes compartidos (variables o canales de comunicación), que traen la necesidad de sincroniźación (ya sea para exclusión mutua o para sincronización condicional).

### Conceptos básicos
Estado: valores de las variables.
Acciones atómicas: se ejecutan de manera indivisible.
Historia: entrelazado de instrucciones atómicas.
|Historias| = (n * m)/(m!)^n donde n = # procesos y m = # inst.atómicas. 
Ejecución: cada ejecución produce una historia.
Indeterminismo: número de historias enorme.
Sincronización: reducir número de historias posibles.

#### Propiedades SAFETY
Corrección parcial: asumiendo que termina, el resultado es correcto.
Exclusión mutua: varios procesos no ejecutan secciones críticas a la vez.

#### Propiedades LIVENESS
Terminación: la longitud de todas las trazas es finita.
No deadlock: todos los procesos entran en la sección crítica alguna vez.

### Paralelización
Objetivo: encontrar formas de paralelizar un programa para que se hagan cómputos a la vez y mejorar el rendomiento.
EJ : Buscar instancias de patrón en fichero PRODUCTOR-CONSUMIDOR.

	String linea;
	bool done = False,buffer_lleno = FALSE,buffer_vacio = FALSE;
	CO {/*productor*/
		String linea2;
		while(true){
			linea2 = getLine(fd);
			if(EOF){
				done = true;
				break;
			}	
			wait buffer_vacio;
			buffer = linea2;
			signal buffer_lleno;
		}
	}
	CO {/*consumidor*/
		String linea1;
		while(true){
			wait buffer_lleno or done == true;
			if(done) break;
			linea1 = buffer;
			signal buffer_vacio;
			encontrado = buscar(linea1,patron);
			if(encontrado) print(linea1);
		}

	}
### Sincronización
EJ : Búsqueda del elemento máximo de un array DOUBLE CHECK.

int m = 0;
CO[i = 0 to n-1]{
	if(a[i] > m)
		<if(a[i] > m) m = a[i];>
}
### Acciones atómias
Atomicidad de grano fino: atomicidad de HARDWARE.
#### At-most-once
Referencia crítica(RC): referencia modificada por otro proceso.
x = e cumple AT-MOST-ONCE si:
	- e contiene como máximo una RC y x no leída por otros procesos.
	- e nno contiene RC y x puede ser leída.
x = e parece ATÓMICA si cumple at-most-once. (<e> = e)
#### <AWAIT(B) S;>
Se adquiere el LOCK cuando B = TRUE.
EJ:
	
	int buf = 0, p = 0, c = 0;
	process Productor{
		int a[n] = inicializa();
		while(p < n){
			<await (p == c);>
			buf = a[p];
			p = p + 1;
		}
	}
	process Consumidor{
		int b[n];
		while(c < p){
			<await (c < p);>
			buf = b[c];
			c = c + 1;
		}
	}
### Semántica de programas concurrentes
Lógica de programación: sistema lógico formal que permite establecer y demostrar propiedades de los programas.
### Propiedades de seguridad y viveza
#### Propiedades de viveza:
La mayoría dependen de la noción de "justicia" (todos los procesos tienen oportunidad de ejecutar).
Política de planificación: cuando hay varios procesos con A.AT. que se podrían ejecutar, DETERMINA CUÁL.
#### Grados de justicia
Incondicional: toda A.AT. incondicional será eventualmente ejecutada (RRobin y Sch.Paralelo).
Débil: incondicional y <await(B) S;> es elegible se ejecuta suponiendo que B se hace TRUE y permanece TRUE hasta que se comprueba (RR y Time Slicing).
Fuerte: incondicional y <await (B) S;> es elegible si B se pone cierto un nº INF de veces.
#### Conclusiones
Imposible práctica y fuertemente justa.
Alternar acciones entre procesos es fuertemente justa.
RoundRobin y TimeSlicing son prácticas pero no FUERTEMENTE justas.

