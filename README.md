# Scientific Calculator (Java)

A complete scientific calculator built in the Java ecosystem using **Maven + Java Swing**.

## Features

- Native desktop GUI via Swing with system look & feel
- Scientific functions:
  - `sin`, `cos`, `tan`
  - `asin`, `acos`, `atan`
  - `log` (base 10), `ln` (natural log), `sqrt`, `abs`
- Arithmetic operations: `+`, `-`, `*`, `/`, `^`
- Constants: `pi`, `e`
- Factorial: `!`
- Parentheses support
- Unary minus support (e.g., `-5+2`, `sin(-30)`)
- Calculation history with quick `ANS` recall

## Requirements

- Java 17+
- Maven 3.8+

## Run

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.example.scicalc.ScientificCalculatorApp"
```

Or package and run the jar:

```bash
mvn package
java -jar target/scientific-calculator-1.0.0.jar
```

## Expression Notes

- Trigonometric inputs are interpreted in **degrees**.
- Inverse trig outputs are returned in **degrees**.
- Factorial is defined for non-negative integers up to 170.
