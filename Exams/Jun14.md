# Examen Programación Concurrente 
## Junio 2014

### (2 puntos)
- Considera el siguiente fragmento de código:

        int x = 0;
        CO S1: <await (x != 0) x = x - 2; >
        // S2: <await (x == 0) x = x + 3; >
        // S3: <await (x == 0) x = x + 2; >
        OC

#### 1.1. Este programa puede terminar o bloquearse. ¿Qué orden de ejecución lleva a su terminación y cuál es el valor final de x en ese estado?
La traza es S3, S1, S2.
El valor final de `x = 3`.

#### 1.2. ¿Qué orden de ejecución causa el bloqueo y cuál es el valor de x en ese estado?
La traza es S2, S1, S3.
El valor final de `x = 1`.

#### 1.3. Reemplaza las instrucciones **await** por **if** y elimina los "<>" que denotan ejecución atómica. Solo lectura y escritura de variable son operaciones atómicas. ¿Cuáles son los posibles valores finales de x?
El código sería algo así:

        int x = 0;
        CO S1: if (x != 0) x = x - 2;
        // S2: if (x == 0) x = x + 3;
        // S3: if (x == 0) x = x + 2;
        OC
Posibles valores de x:
    - `x = 0`
    - `x = 1`
    - `x = 2`
    - `x = 3`
    - `x = 5`
    
### 2. (2 puntos)
- Asume la existencia de la siguiente instrucción **Exchange** que atómicamente intercambia el valor de dos posiciones de memoria que recibe como parámetro:

        Exchange(int var1, int var2)
            < int temp; temp = var1; var1 = var2; var2 = temp; >

- Desarrolla una solución al *problema de la sección crítica* usando **Exchange**. Concretamente, proporciona el código para **CSEntry** y **CSExit** que utilizan la variable **lock** declarada a continuación. La solución no tiene que ser justa.

        int lock = 0;
CSEntry
    
    int myLock = 1;
    Exchange(lock, myLock);
    while(myLock == 1){
        while(lock == 1)
            ;
        Exchange(myLock, lock);
    }

CSExit
    
    lock = 1;
    
### 3. (2 puntos)
- Considera el siguiente par de primitivas de paso de mensaje:
    - **broadcast(m)**:  proporciona copia de **m** a todos los procesos que están actualmente escuchando.
    - **listen(x)**: espera al *siguiente* **broadcast**, toma una copia del mensaje y lo almacena en la variable local **x**.

- Un proceso productor utiliza **broadcast** para dar un mensaje a todos los consumidores que escuchan en ese momento. Si no hay, **broadcast** no tiene efecto. Si hay consumidores escuchando, todos ellos deben coger la copia de **m**.
- Completa la siguiente implementación de **broadcast** y **listen** utilizando variables compartidas y semáforos. Asume que los mensajes son números enteros. Puede haber varios procesos productores y varios consumidores, pero únicamente una operación **broadcast** en ejecución cada vez. Declara e inicializa los semáforos que utilices.
- RECORDATORIO: Las operaciones sobre un semáforo *s* son:
    - P(s) espera a valor positivo y decrementa (down())
    - V(s) aumenta contador del semáforo (up())

            int buffer, n_proc_waiting = 0;
            sem lock = 1, waiting = 0;
            
            broadcast(m){
                    /* Adquiero LOCK */
                P(lock);
                    /* Escribo el mensaje en buffer */
                buffer = m;
                    /* Despierto posibles*/
                if(n_proc_waiting > 0){
                    n_proc_waiting--;
                    V(waiting);           /* Paso de testigos: paso LOCK al proceso que espera*/
                }
                    /* Libero LOCK */
                else V(lock);
            }
            
            listen(m){
                    /* Adquiero LOCK para escribir en variable compartida */
                P(lock);
                n_proc_waiting++;
                V(lock);
                    /* Suelto LOCK y me quedo esperando... */
                P(waiting);
                    /* Leo BUFFER */
                m = buffer;
                if(n_proc_waiting > 0){
                        /* Si hay procesos dormidos decremento contador y LE PASO EL TESTIGO */
                    n_proc_waiting--;
                    V(waiting);
                }
                    /* Si no libero LOCK*/
                else V(lectura);
            }
            
### 4. (2.5 puntos)
- Implementa un monitor **en Java** para crear grupos de dos alumnos. Cada alumno tiene identificador único. Hay N alumnos, donde N es *par constante*.
- Existe solo una operación **formarGrupo** con este perfil `int formarGrupo(int ID)` donde **ID** es un identificador de un alumno, y el retorno el identificador de la **pareja asignada**.
- Por cada par de llamadas **formarGrupo** devuelve la identidad de su pareja a cada una de ellas.
- NOTA: **formarGrupo** NO debe esperar las *N* llamadas, sino que se procesa dos llamadas cada vez.
- IDEA: Debes tener en cuenta que con la disciplina de señalización "Señalizar y Continuar" de Java, una nueva llamada puede ser procesada antes que un proceso que estaba esperando y ya ha sido señalizado.
- RECORDATORIO: Puedes realizar una implementación basada en el uso de *synchronized*, *wait* y *notify*, o alternativamente utilizar *locks & conditions* de Java (utilizando los métodos *lock* y *unlock* sobre las variables de tipo *Condition* los métodos *await* y *signal*).

        class Monitor {
                /* num : contador de índices      */
                /* ID[]: array de identificadores */
            private int num = 0, ID[N] = ([N]0);
            
            synchronized int formarGrupo(int ID){
                int indice;
                num++;
                    /* indice : siguiente al contador */
                indice = num;
                    /* asigno el contador al array de índices */
                IDs[indice] = ID;
                    /* si indice es el primero de los dos compañeros */
                if((indice % N) == 1){  /* primero */
                    wait();
                        /* se ha asignado a su compañero en la siguiente posición asi que se devuelve*/
                    return ID[indice + 1];
                }
                else {  /* segundo */
                    int ret = ID[indice];
                    notify();
                    return ret;
                }
            }
        }
        
### 5. (1.5 puntos)
- Implementa una **barrera de diseminación** para N procesos utilizando **paso de mensajes asíncrono**. Debes declarar los canales que necesites (usando *Chan*) y utilizar las primitivas *send* y *receive* para señalizar la llegada a la barrera y recibir las señalizaciones por parte de los otros procesos.
        
            /* El canal NO TIENE PARÁMETROS (solo avisa de llegada)*/
        Chan barrera[N]();   
        
        process P[i = 1 to N]{
            int j = 1;
                
                /* Mientras no se llegue al último proceso*/
            while(j < N){
                    /* Espero a todos los procesos A MI DERECHA */
                send barrera[i + j]();
                    /* Espero testigo de mis procesos A LA IZQUIERDA */
                receive barrera[j]();
            }
        }