# Programación Concurrente
## Tema 3 : Cerrojos y Barreras (El problema de la SC)
### Introducción
Los programas concurrentes emplean sincronización por exclusión mutua y sincronización condicional.
Vamos a examinar como programar este tipo de sincronización mediante la examinación de dos problemas típicos: secciones críticas y barreras.
### El problema de la SC
process CS[i = 1 to n]{
	while(true){
		CSEntry;
		CS;
		CSExit;
		no_CS;
	}
}
El objetivo es garantizar cuatro propiedades:
	1) Exclusión mutua: un proceso como máximo ejecutando CS.
	2) No deadlock: si dos procesos intentan entrar, sólo y COMO MÍNIMO 1 lo conseguirñá.
	3) No retraso.
	4) Live: un proceso que intenta entrar lo hará en algún momento.

### SC: Spin Lock
Equivalente a <await(B) SC;>.

bool lock = false;
process CS1{
	while(true){
		<await (!lock) lock=true;>	(CSENTRY)
		SC;
		lock = false;				(CSEXIT)
		no_CS;
	}
}

#### SOL1: Test & Test & Set
bool TS(bool & lock){
	bool initial = lock;		
	lock = true;
	return initial;
}

bool lock = false;
process CS[i = 1 to n]{
	while(true){
		while lock
			;
		while(TS(lock))
			while lock
				;
		CS
		lock = false;
		no_CS;
	}
}
1ª COMP: Este while lo ejecutan (sin detenerse) todos los procesos entrantes al principio hasta que uno ejecuta TS(lock).
2ª COMP: El primero en elejutar TS coge el cerrojo y pone lock a false (así, este es el único que sale del 2º while). 
3ª COMP: Comprobación barata para que los procesos que esperen lo hagan ejecutando while lock (mucho MÁS BARATA que T&S).
#### SOL2: Implementación AWAIT
CSEntry;
while(!B){
	CSExit;
	Delay;
	CSEntry;
}
CS;
CSExit;
no_CS;
### SC: Soluciones justas
Las soluciones anteriores requieren un planificador fuertemente justo y nuestras soluciones no pueden depender del hardware.
#### SOL1: Tie-Break
process CS[i = 1 to n]{
	while(true){
		for[j = 1 to n]{
			in[i] = j;
			last[j] = i;
			for[k = 1 to numEtapas with k != i]
				while(in[k] > in[i] &&
					  last[j] == i)
					  ;
					 // Esperamos si hay un proceso K por delante
					 // Y si yo soy el último en etapa J
		}
	}
} 
#### SOL2: Ticket
int FA(int var, int incr){
	int tmp = var;
	var = var + incr;
	return tmp;
}
##### Bajo nivel
int turn[1..n] = ([n]0);
int next = 1;
int number = 1;

process CS[i = 1 to n]{
	while(true){
		turn[i] = FA(number,1);
		while(turn[i] != next)
			;
		CS;
		next = next + 1;
		no_CS;
	}
}
##### Alto nivel
Hay que sustituir Fetch&Add por ejecución atómica en <turn[i] = number; number = number + 1;>.

#### SOL3: Bakery
Hay que inventarse operador >>:
	(a,b) >> (c,d)     = (a > c) or (a == c ^ b > d)
int turn[1..n] = ([n]0);
int next = 1;
int number = 1;

process CS[i = 1 to n]{
	while(true){
		turn[i] = 1;
		turn[i] = max(turno) + 1;
		for[j = 1 to n with j != i]
			while(turn[j] != 0 &&
				  (turn[i],i) >> (turn[j,j]))
				;
	}
}

Posiblemente este sea el mejor algoritmo: es casi justo y muy poco costoso.
### Sincronización con barrera
Los algoritmos paralelos iterativos típicamente iteran sobre parte de los datos (una it. puede depender de la anterior).
Podríamos implementarlo con CO
while(true){
	CO[i = 1 to n]
		código iteración i
	OC;
}
O poniendo una barrera (punto de parada).
process worker[i=1 to n]{
	while(true){
		código iteración i
		barrera terminación n tareas
	}
}
#### Contadores compartidos
Utilizando una variable compartida.

while(true){
	código iteración i
	FA(count,1);
	<await (count == n) ;>
	borrado de count
}

Solución muy mala.
#### Banderas y coordinadores
##### SOL1: WORKER y COORDINADOR
int arrive[1..n] = ([n]0);
int continue[1..n] = ([n]0);
process worker[i = 1 to n]
while(true){
	código iteración i
	arrive[i] = 1;
	while(continue[i] == 0)
		;
	continue[i] = 0;
}

process coord
while(true){
		// 1º ESPERO QUE LLEGUEN TODOS
	for[i = 1 to n]{
		while(arrive[i] == 0)
			;
		arrive[i] = 0;
	}
		// 2º INDICO QUE ARRANQUEN
	for[i = 1 to n]
		continue[i] = 1;
}
#### SOL2: Sin worker
Se trata de un árbol de procesos, en el cual los hijos avisan al padre de que han llegado y el padre avisa a los hijos de que continuen.
Así, podemos distinguir tres tipos de procesos:
	-Raíz R.
	-Nodo I.
	-Hoja L.

process R
while(true){
	while(arrive[left(R)] == 0)
		;
	arrive[left(R)] = 0;
	while(arrive[right(R)] == 0)
		;
	arrive[right(R)] = 0;
	continue[left(R)] = 1;
	continue[right(R)] = 1;
}

process I
while(true){
	while(arrive[left(I)] == 0)
		;
	arrive[left(I)] = 0;
	while(arrive[right(I)] == 0)
		;
	arrive[right(I)] = 0;
	arrive[I] = 1; 
	while(continue[I] == 0)
		;
	continue[I] = 0;
	continue[left(I)] = 1;
	continue[right(I)] = 1;
}

process L
while(true){
	arrive[L] = 1;
	while(continue[L] == 0)
		;
	continue[L] = 0;
}
#### Barreras simétricas
##### SOL1: Barrera de mariposa
Se usa cuando el número de procesos es potencia de 2, y esta potencia es el número de fases de la barrera.
En cada fase s € {0..}, cada proceso se comunica con su proceso a 2^s de distancia a la derecha.
##### SOL2: Barrera de diseminación
Se usa para cuando no son potencias de dos, y se usa la cota superior del logaritmo del número de procesos.
process worker[i = 1 to n]{
	for[s = 1 to numEtapas]{
		1) MARCAR arrive[i]
		2) DETERMINAR j a quien esperar (2^s)
		3) ESPERAR a j
	}
}

process worker[i = 1 to n]{
	for[s = 1 to numEtapas]{
		arrive[i] = arrive[i] + 1;
		# determinar j en etapa s
		while(arrive[j] < arrive[i])
			;
	}
}
### Algoritmos de datos paralelos
#### Computaciones de prefijos paralelos

process Sum[i = 0 to n-1]{
	int d = 1;			# distancia a j
	sum[i] = a[i];
	while(d < n){		# cota superior log n
		old[i] = sum[i];
		BARRERA
		if(i - d >= 0)	# determinar si existe a[j]
			sum[i] = old[i - d] + a[i];
		BARRERA
		d = d + d;		# potencia de 2
	}
}

EJ: ECUACIÓN DE LAPLACE
process G[i = 1 to n, j = 1 to n]
while (no_converge){
	newgrid[i,j] = (grid[i-1,j] +
					grid[i+1,j] + 
					grid[i,j-1] +
					grid[i,j+1]) / 4;
	BARRERA
	comprobar convergencia
	grid[i,j] = newgrid[i,j];
	BARRERA
}