package org.ankur;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.json.*;

public class Main {

    public static void main(String[] args) throws Exception {
        String[] testFiles = { "testcase1.json", "testcase2.json" };
        for (String file : testFiles) {
            JSONObject input = new JSONObject(Files.readString(Path.of(file)));
            int n = input.getJSONObject("keys").getInt("n");
            int k = input.getJSONObject("keys").getInt("k");

            // Parse x and y values
            Map<Integer, Point> idToPoint = new HashMap<>();
            for (String key : input.keySet()) {
                if (key.equals("keys")) continue;
                int x = Integer.parseInt(key);
                String base = input.getJSONObject(key).getString("base");
                String value = input.getJSONObject(key).getString("value");
                BigInteger y = new BigInteger(value, Integer.parseInt(base));
                idToPoint.put(x, new Point(BigInteger.valueOf(x), y));
            }

            // Track secret frequencies and valid shares
            Map<BigInteger, Integer> secretFrequency = new HashMap<>();
            Map<BigInteger, List<Set<Integer>>> secretToCombos = new HashMap<>();

            List<List<Integer>> combinations = combinations(new ArrayList<>(idToPoint.keySet()), k);

            for (List<Integer> comboIDs : combinations) {
                List<Point> subset = new ArrayList<>();
                for (int id : comboIDs)
                    subset.add(idToPoint.get(id));
                try {
                    BigDecimal[] coeffs = solvePolynomial(subset);
                    BigInteger constantTerm = coeffs[0].toBigInteger();

                    secretFrequency.put(constantTerm, secretFrequency.getOrDefault(constantTerm, 0) + 1);
                    secretToCombos.computeIfAbsent(constantTerm, x -> new ArrayList<>()).add(new HashSet<>(comboIDs));

                } catch (Exception ignored) {}
            }

            // Determine correct secret
            BigInteger correctSecret = Collections.max(secretFrequency.entrySet(), Map.Entry.comparingByValue()).getKey();

            // Collect all valid share IDs (those in combos that gave the correct secret)
            Set<Integer> validIDs = new HashSet<>();
            for (Set<Integer> ids : secretToCombos.get(correctSecret)) {
                validIDs.addAll(ids);
            }

            // All IDs
            Set<Integer> allIDs = new HashSet<>(idToPoint.keySet());
            Set<Integer> invalidIDs = new HashSet<>(allIDs);
            invalidIDs.removeAll(validIDs);

            // Output
            System.out.println("Secret from " + file + ": " + correctSecret);
            System.out.println("Valid Share IDs: " + validIDs);
            System.out.println("Invalid Share IDs: " + invalidIDs);
            System.out.println("====================================");
        }
    }

    static BigDecimal[] solvePolynomial(List<Point> points) {
        int k = points.size();
        BigDecimal[][] matrix = new BigDecimal[k][k + 1];

        for (int i = 0; i < k; i++) {
            BigDecimal x = new BigDecimal(points.get(i).x);
            BigDecimal y = new BigDecimal(points.get(i).y);
            for (int j = 0; j < k; j++) {
                matrix[i][j] = x.pow(j);
            }
            matrix[i][k] = y;
        }

        // Gaussian Elimination
        for (int i = 0; i < k; i++) {
            BigDecimal div = matrix[i][i];
            for (int j = 0; j <= k; j++)
                matrix[i][j] = matrix[i][j].divide(div, 50, BigDecimal.ROUND_HALF_UP);

            for (int l = 0; l < k; l++) {
                if (l != i) {
                    BigDecimal factor = matrix[l][i];
                    for (int j = 0; j <= k; j++) {
                        matrix[l][j] = matrix[l][j].subtract(factor.multiply(matrix[i][j]));
                    }
                }
            }
        }

        BigDecimal[] solution = new BigDecimal[k];
        for (int i = 0; i < k; i++)
            solution[i] = matrix[i][k];

        return solution;
    }

    static List<List<Integer>> combinations(List<Integer> input, int k) {
        List<List<Integer>> result = new ArrayList<>();
        combineHelper(input, 0, k, new ArrayList<>(), result);
        return result;
    }

    static void combineHelper(List<Integer> input, int start, int k, List<Integer> temp, List<List<Integer>> result) {
        if (temp.size() == k) {
            result.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < input.size(); i++) {
            temp.add(input.get(i));
            combineHelper(input, i + 1, k, temp, result);
            temp.remove(temp.size() - 1);
        }
    }

    static class Point {
        BigInteger x, y;
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }
}
