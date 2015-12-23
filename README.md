# SonarQubeExt
useful rules on developing custom software for the Italian public administration

Regole sonar custom 
Il plug-in sviluppato è: ALM-sonar-plugin-1.0.0.0.jar 
Le regole che vengono inserite nel profile sono le seguenti:
1.	Densità dei commenti del software sviluppato
2.	Linee di codice inerte
3.	Dipendenza di una classe dai suoi child
4.	Metodi implementati in una Classe
5.	Complessità Ciclomatica di una Classe
6.	Grado di Coesione dei Metodi di una Classe
7.	RCF (Risposta per Classe)
8.	Depth (Profondità dell’Albero di Eredità)
9.	CBO (Accoppiamento tra classi)


1.	Densità dei commenti del software sviluppato

Aggiungere un alert in modo che scatti se i commenti del codice sono inferiori al 8% rispetto a tutte le righe di codice.
Nella schermata di Quality Profiles andare nel tag Alerts e creare l’alert.
  
2.	Linee di codice inerte
Aggiungere delle regole per gli elementi del codice che non vengono utilizzate a runtime.
Nella schermata di Quality Profiles andare nel tag Coding Rules e aggiungere tutte le regole che riguardano gli elementi “unused”.
 
Impostare il livello di severità a “Bloker”. Poi creare un altro alert che se trova una sola violazione scatta.
 
3.	Dipendenza di una classe dai suoi child
E’ attiva la regola “Dep On Child” su Sonar
 
4.	Metodi implementati in una Classe
E’ attiva la regola “Maximum Methods Count Check” su Sonar. Essa conta il numero di metodi che contiene ogni classe.
 
5.	Complessità Ciclomatica di una classe
Su Sonar è già implementata una regola Cyclomatic Complexity che calcola la complessità ciclomatica di una classe Java. Attivare la suddetta regola ed impostare il valore di soglia a 60 (come specificato nel documento delle metriche).
 
6.	Grado di Coesione dei Metodi di una Classe
E’ attiva la regola “Coesione dei metodi” su Sonar
 
7.	RCF (Risposta per classe)
E’ attiva la regola “RFC” su Sonar. Se il numero dei metodi che possono essere invocati in risposta ad un messaggio ricevuto è maggiore di 50 la regola lancia una violazione.
 



8.	Depth (Profondità dell’Albero di Eredità)
E’ attiva la regola “Profondità classe” su Sonar. Se la classe ha un albero di eredità più profondo di 5 nodi la regola lancia una violazione.
 
9.	CBO (Accoppiamento tra Classi)
Su Sonar è già implementata una regola Coupling between objects che calcola il numero di classi alle quali una data classe Java è accoppiata secondo il paradigma Object Oriented. Attivare la suddetta regola ed impostare il valore di soglia a 4 (come specificato nel documento delle metriche).
 
