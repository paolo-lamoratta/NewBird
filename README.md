# NewBird - Capodanno Edition 🦅🎸

Questa è un'applicazione Android nativa progettata per un unico, glorioso scopo: riprodurre "Free Bird" in modo che il leggendario assolo (al minuto 4:55) inizi esattamente allo scoccare della mezzanotte di Capodanno.

## Requisiti

- Android Studio o SDK Android installato.
- Un dispositivo Android o Emulatore.

## Struttura del Progetto

Il progetto è strutturato come una standard app Android Kotlin con Jetpack Compose.
Il file audio si trova in `app/src/main/res/raw/free_bird.mp3`.

## Come Compilare ed Eseguire

1.  Apri questa cartella in **Android Studio**.
2.  Attendi la sincronizzazione di Gradle.
3.  Collega il tuo telefono o avvia un emulatore.
4.  Premi **Run** (Play button).

## Come Funziona

L'app calcola la prossima mezzanotte.
- **Target**: 00:00:00 del giorno successivo (es. 1 Gennaio).
- **Offset**: 4 minuti e 55 secondi.
- **Start Time**: 23:55:05.

Se apri l'app prima delle 23:55:05, vedrai un conto alla rovescia.
Se apri l'app tra le 23:55:05 e la mezzanotte, la canzone partirà immediatamente sincronizzata al punto giusto (seek).

Buon anno e buon assolo!
