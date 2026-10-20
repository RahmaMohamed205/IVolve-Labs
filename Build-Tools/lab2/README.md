# Lab 2: Building and Packaging a Java Application with Gradle

## Objective
Build, test, and package an application using Gradle.

Source code: https://github.com/Ibrahim-Adel15/CalculatorGradleApp.git

## Prerequisites
- WSL2, Java 17, Gradle, Git

## Steps

**1. Clone the source code**
```bash
git clone https://github.com/Ibrahim-Adel15/CalculatorGradleApp.git
cd CalculatorGradleApp
```

**2. Run unit tests**
```bash
gradle test
```
![gradle test](Screenshots/1-gradle-test.png)

**3. Build the application**
```bash
gradle build
```
The artifact is generated at `build/libs/calculator.jar`.

![gradle build](Screenshots/2-gradle-build.png)

**4. Run the application**
```bash
java -jar build/libs/calculator.jar
```
![run app](Screenshots/3-run-app.png)

## Result
- All unit tests passed.
- The JAR file was built successfully.
- The application ran and worked as expected.