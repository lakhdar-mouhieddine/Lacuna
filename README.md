# Lacuna

## Compilation

Pour compiler le jeu : `javac -d bin src/lacuna/model/*.java src/lacuna/model/AI/*.java src/lacuna/view/*.java src/lacuna/view/menu/*.java src/lacuna/view/board/*.java src/lacuna/controller/*.java src/lacuna/network/*.java`
Pour executer le jeu : `java -cp bin lacuna.view.MainFrame`

## Création du JAR

Pour créer un fichier JAR exécutable (`Lacuna.jar`) :

1. Créez un fichier temporaire nommé `manifest.txt` avec le contenu suivant (assurez-vous d'avoir une ligne vide à la fin) :
   ```text
   Manifest-Version: 1.0
   Main-Class: lacuna.view.MainFrame

   ```

2. Assemblez le JAR à l'aide de l'outil `jar` en incluant le dossier des ressources (`assets`) :
   ```bash
   jar cfm Lacuna.jar manifest.txt -C bin . -C . assets
   ```

3. Vous pouvez ensuite supprimer le fichier `manifest.txt` temporaire.

## Exécution (JAR)

Pour exécuter le jeu à partir du fichier JAR :
```bash
java -jar Lacuna.jar
```