# Secret Finder - Shamir's Secret Sharing (Catalog Assignment)

This project solves a simplified version of the [Shamir's Secret Sharing](https://en.wikipedia.org/wiki/Shamir%27s_Secret_Sharing) scheme. The task is to reconstruct the **secret (constant term)** of a hidden polynomial using a minimum number of encoded shares, and identify any incorrect (invalid) shares.

## Problem Summary

- You're given `n` shares in a JSON file.
- Each share has:
  - A key `x` (e.g., `"2"`)
  - A `value` encoded in a given `base` (e.g., `"value": "111"` with `"base": "2"`)
- You must:
  - Decode the base-encoded values to get points (x, y)
  - Use any `k` out of `n` points to reconstruct a degree `k-1` polynomial
  - Identify the constant term (`c`) of the polynomial → this is the **secret**
  - Identify **invalid shares** (those that never appear in correct combinations)

---

## Features

- Reads input from 2 JSON test files.
- Supports large base conversions and big integers.
- Automatically reconstructs the secret using matrix-based polynomial solving.
- Identifies valid and invalid shares using all k-size combinations.
- Clean console output of results.

---

## Folder Structure

```secret-finder/
├── pom.xml
├── testcase1.json
├── testcase2.json
└── src/
└── main/
└── java/
└── com/
└── catalog/
└── assignment/
└── App.java
```

---

## Run Instructions

### Prerequisites
- Java 17+
- Maven 3.6+

### Build and Run

```bash
# Compile
mvn compile

# Run
mvn exec:java -Dexec.mainClass="com.catalog.assignment.App"
Output Example
Secret from testcase1.json: 3
Valid Share IDs: [1, 2, 3, 6]
Invalid Share IDs: []

Secret from testcase2.json: 79836264049851
Valid Share IDs: [2, 3, 4, 5, 6, 7, 9, 10]
Invalid Share IDs: [1, 8]
====================================
```
### Techniques Used
Base conversion using BigInteger(value, base)

Matrix-based polynomial interpolation using Gaussian Elimination

BigDecimal precision math to handle large values

Combinatorics to test all combinations of k shares out of n
