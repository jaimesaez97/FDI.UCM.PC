# Examen Programación Concurrente
## 11 Septiembre 2015

### 1. (2.5 puntos)
- Considera el siguiente fragmento de código:

        int x = 1, y = 1, z = 1;
        CO x = y + 1;
        // <y = x + 2;
        // z = x + y;
        // <await (x>0) x = 0; y = 0; z = 0;>
        OC
		
- En los procesos primero y tercero, las acciones atómicas son leer y escribir las variables individuales.
- Los procesos segundo y cuarto son en sí una acción atómica.

#### 1.1. (1.25puntos) ¿Cuántas historias hay para este programa? En concreto, se pide el número total de diferentes órdenes de ejecución. Razona tu respuesta.

El número de diferentes ordenes de ejecuciòn viene dado por la fórmula
    (nºprocesos*nºV.A.)! / (nºprocesos)!(nºV.A.)!

S1,S2,S3,S4, habiendo seis posibilidades de saltar entre S4 y S3:
    - Al leer la variable a escribir.
    - Al leer variable x/y.
    - Al sumarle la constante.
S1,S2,S4,S3.

#### 1.2. ¿Cuáles son los posibles valores finales para cada una de las variables?
- (x,y,z) =
    - (0,2,3)
    - (0,0,0)
    - (0,0,2)
    - (1,0,0)
    - (1,0,1)
    - ()
    
### 2. (2.5 puntos)
- El siguiente código debe implementar una solución al problema de los lectores y escritorers usando el *paso de testigo*. Completa la implementación para dar preferencia a los escritores de manera que:
    1. Los nuevos lectores se retrasan si un escritor está esperando.
    2. Un lector esperando será despertado sólo si no hay ningún escritor esperando.
    
- RECORDATORIO: Las operacioens sobre un semáforo *s* son:
    - P(s) : espera a s > 0 y lo decrementa.
    - V(s) : incrementa s

            int nr = 0, nw = 0, dr = 0, dw =0;
            Sem lock = 1, reading = 0, writing = 0;
            process Reader [i = 1 to N]
                while(true){
                    P(lock);
                    if(nw > 0 || dw > 0){
                        dr = dr + 1;
                        V(lock);
                        P(reading);
                    }
                    nr = nr + 1;
                        
                        /* PASO DE TESTIGO a readers */
                    if(dr > 0){
                        dr = dr - 1;
                        V(reading);
                    } else V(lock);
                    
                    leer();
                    
                    P(lock);
                    nr = nr - 1;
                    
                        /* PASO DE TESTIGO a writers*/
                    if(nr == 0 && dw > 0){
                        dw = dw - 1;
                        V(writing);
                    } else V(lock);
                }
            
            process Writer [i = 1 to M]
                while(true){
                    P(lock);
                        /* Si ya hay un proceso suelto LOCK, incremento dw y me bloqueo en writing */
                    if(nw > 0 || nr > 0){
                        dw = dw + 1;
                        V(lock);
                        P(writing);
                    }
                    
                    nw = nw + 1;
                    
                        /* Despertado EN CADENA */
                    if(dw > 0){
                        dw = dw - 1;
                        V(writing);
                    }
                    
                    escribir();
                    
                    P(lock);
                    nw = nw - 1;
                    
                        /* PASO DE TESTIGO a lector/escritor */
                    if(dw > 0){
                        dw = dw - 1;
                        V(writing);
                    } else if(dr > 0) {
                        dr = dr - 1;
                        V(reading);
                    } else V(lock);
                
                }

## 3. (3 puntos)
- Una operación de reducción toma N valores y los combina en un único valor usando algún operador binario como la suma. Asume que tienes un programa con N procesos. Implementa monitor **en Java** que computa la reducción-suma de N valores. El monitor tiene una única operación con el siguiente perfil: `int reduce(int valor)`. Es llamada N veces, una por proceso, y a cada proceso le devuelve la suma de los N valores.
- RECORDATORIO: Puedes realizar una implementación basada en el uso de *synchronized*, *wait*, *notify*, *notifyAll* o usar *locks & conditions* de Java.

        class Monitor {
            private int num = 0;
            private int totalValue = 0;
            
            synchronized int reduce (int valor){
                int indice;
                num++;
                indice = num**;
                
                totalValue = totalValue + valor;
                (indice == N-1)? notifyAll() : wait();  /* Último proceso NOTIFICA, el resto ESPERAN */
                return totalValue;
            }
        }

## 4. (2 puntos)
- Desarrolla unasolución al problema de la cuenta corriente visto en clase, pero utilizando primitivas *rendezvous*. Para ello representa el banco como un proceso. Escribe el código para el banco y el código que un cliente ejecutaría para hacer un ingreso y una extracción.
- RECORDATORIO: El proceso servidor en un módulo realiza un rendezvous ejecutando `in opname(identificadores) [and B1 by e1] -> S; ni`. Las partes opcionales aparecen entre corchetes. Los clientes invocan las operaciones ejecutando **call**.

¿Lo hemos dao?