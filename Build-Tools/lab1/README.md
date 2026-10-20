# Lab 1: Building and Packaging a Java Application with Maven

## Objective
Build, test, and package a Java application using Maven.

Source code: https://github.com/Ibrahim-Adel15/CalculatorMavenApp.git

## Prerequisites
- WSL2, Java 17, Maven, Git

## Steps

**1. Clone the source code**
```bash
git clone https://github.com/Ibrahim-Adel15/CalculatorMavenApp.git
cd CalculatorMavenApp
```

**2. Run unit tests**
```bash
mvn test
```

**3. Build the application**
```bash
mvn package
```
The artifact is generated at `target/calculator.jar`.

**4. Run the application**
```bash
java -jar target/calculator.jar
```

## Result
- All unit tests passed.
- The JAR file was built successfully.
- The application ran and worked as expected.