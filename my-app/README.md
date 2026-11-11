# My App

This project can be used as a starting point to create your own Vaadin application with Spring Boot.
It contains all the necessary configuration and some placeholder files to get you started.

## Running the application

Open the project in an IDE. You can download the [IntelliJ community edition](https://www.jetbrains.com/idea/download) if you do not have a suitable IDE already.
Once opened in the IDE, locate the `Application` class and run the main method using "Debug".

For more information on installing in various IDEs, see [how to import Vaadin projects to different IDEs](https://vaadin.com/docs/latest/getting-started/import).

If you install the Vaadin plugin for IntelliJ, you should instead launch the `Application` class using "Debug using HotswapAgent" to see updates in the Java code immediately reflected in the browser.

## Tests ausführen (Modul 450, LB1)

Alle Befehle im Ordner `my-app` ausführen. Falls `mvnw` nicht funktioniert, das globale `mvn` verwenden.

### Unit-Tests (JUnit 5 + Mockito)

```
mvn test
```

Einzelne Testklassen:

```
mvn test -Dtest=SkillServiceTest,BasketballStatsServiceTest
```

Im Terminal steht pro Testklasse eine Zeile (ohne `-q` starten, sonst wird die Ausgabe unterdrückt):

```
Running com.example.application.services.SkillServiceTest
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0 -- in ...SkillServiceTest
```

Am Ende folgt die Gesamtzusammenfassung (`Tests run: ..., Failures: 0`) und `BUILD SUCCESS`.
Die Einzelergebnisse stehen auch in `target/surefire-reports/`.

| Testklasse | Getestete Unit | Art |
|---|---|---|
| `services/SkillServiceTest` | `SkillService` | Mocking (Repository gemockt) |
| `services/BasketballStatsServiceTest` | `BasketballStatsService` | Mocking (Repository gemockt) |
| `data/SamplePersonTest` | `SamplePerson`, `AbstractEntity` | Logik / Entität |
| `data/SkillTest` | `Skill` | Entität |
| `data/BasketballStatsTest` | `BasketballStats` | Entität |

### Code Coverage (JaCoCo)

Läuft automatisch bei `mvn test`. Report im Browser öffnen:

```
target/site/jacoco/index.html
```

### Mutation Testing (PIT)

```
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

Im Terminal erscheint am Ende die Statistik, z. B. `Generated 30 mutations Killed 30 (100%)`.
Der HTML-Report mit allen Mutationen pro Klasse und Zeile (grün = getötet, rot = überlebt):

```
target/pit-reports/index.html
```

Getestete Klassen für PIT sind in der `pom.xml` unter `pitest-maven` (`targetClasses` / `targetTests`) festgelegt:
`SamplePerson`, `AbstractEntity`, `SkillService`.

### Tests in IntelliJ (GUI)

- **Einzelne Klasse:** Testdatei öffnen und auf den grünen Pfeil neben `class ...Test` klicken, dann *Run*.
- **Alle Tests:** Rechtsklick auf den Ordner `src/test/java`, dann *Run 'All Tests'*. Unten im Run-Fenster erscheint der Testbaum mit grünen und roten Häkchen pro Klasse und Methode.
- **Mit Coverage:** Rechtsklick auf `src/test/java`, dann *Run 'All Tests' with Coverage*. Die Coverage wird farbig im Code und im Coverage-Fenster angezeigt.
- **Über Maven:** Rechts im Reiter *Maven* unter `Lifecycle` auf `test` doppelklicken. PIT liegt unter `Plugins`, `pitest`, `pitest:mutationCoverage` (vorher einmal `test-compile` ausführen).
- Für Mutation Testing gibt es in IntelliJ keine eingebaute GUI. Der Report wird im Browser geöffnet: Rechtsklick auf `target/pit-reports/index.html`, dann *Open in*, dann *Browser*.

## Deploying to Production

The project is a standard Maven project. To create a production build, call 

```
./mvnw clean package -Pproduction
```

If you have Maven globally installed, you can replace `./mvnw` with `mvn`.

This will build a JAR file with all the dependencies and front-end resources,ready to be run. The file can be found in the `target` folder after the build completes.
You then launch the application using 
```
java -jar target/shootingtrackerapplication-1.0-SNAPSHOT.jar
```

## Project structure

- `MainLayout.java` in `src/main/java` contains the navigation setup (i.e., the
  side/top bar and the main menu). This setup uses
  [App Layout](https://vaadin.com/docs/components/app-layout).
- `views` package in `src/main/java` contains the server-side Java views of your application.
- `views` folder in `src/main/frontend` contains the client-side JavaScript views of your application.
- `themes` folder in `src/main/frontend` contains the custom CSS styles.

## Useful links

- Read the documentation at [vaadin.com/docs](https://vaadin.com/docs).
- Follow the tutorial at [vaadin.com/docs/latest/tutorial/overview](https://vaadin.com/docs/latest/tutorial/overview).
- Create new projects at [start.vaadin.com](https://start.vaadin.com/).
- Search UI components and their usage examples at [vaadin.com/docs/latest/components](https://vaadin.com/docs/latest/components).
- View use case applications that demonstrate Vaadin capabilities at [vaadin.com/examples-and-demos](https://vaadin.com/examples-and-demos).
- Build any UI without custom CSS by discovering Vaadin's set of [CSS utility classes](https://vaadin.com/docs/styling/lumo/utility-classes). 
- Find a collection of solutions to common use cases at [cookbook.vaadin.com](https://cookbook.vaadin.com/).
- Find add-ons at [vaadin.com/directory](https://vaadin.com/directory).
- Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/vaadin) or join our [Forum](https://vaadin.com/forum).
- Report issues, create pull requests in [GitHub](https://github.com/vaadin).
