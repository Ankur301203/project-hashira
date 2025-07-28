package org.ankur;

import java.math.BigDecimal;
import java.math.BigInteger;
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

            Map<Integer, Point> idToPoint = new HashMap<>();
            for (String key : input.keySet()) {
                if (key.equals("keys")) continue;
                int x = Integer.parseInt(key);
                String base = input.getJSONObject(key).getString("base");
                String value = input.getJSONObject(key).getString("value");
                BigInteger y = new BigInteger(value, Integer.parseInt(base));
                idToPoint.put(x, new Point(BigInteger.valueOf(x), y));
            }
            Map<BigInteger, Integer> freq = new HashMap<>();
            Map<BigInteger, List<Set<Integer>>> secretToCombos = new HashMap<>();

            List<List<Integer>> combinations = comb(new ArrayList<>(idToPoint.keySet()), k);

            for (List<Integer> currList : combinations) {
                List<Point> subset = new ArrayList<>();
                for (int id : currList)
                    subset.add(idToPoint.get(id));
                try {
                    BigDecimal[] coeffs = solve(subset);
                    BigInteger constantTerm = coeffs[0].toBigInteger();

                    freq.put(constantTerm, freq.getOrDefault(constantTerm, 0) + 1);
                    secretToCombos.computeIfAbsent(constantTerm, x -> new ArrayList<>()).add(new HashSet<>(currList));

                }
                catch (Exception ignored) {}
            }
            BigInteger ansSec = Collections.max(freq.entrySet(), Map.Entry.comparingByValue()).getKey();
            Set<Integer> validIDs = new HashSet<>();
            for (Set<Integer> ids : secretToCombos.get(ansSec)) {
                validIDs.addAll(ids);
            }
            Set<Integer> allIDs = new HashSet<>(idToPoint.keySet());
            Set<Integer> invalidIDs = new HashSet<>(allIDs);
            invalidIDs.removeAll(validIDs);

            System.out.println("Secret from " + file + ": " + ansSec);
            System.out.println("Valid Share IDs: " + validIDs);
            System.out.println("Invalid Share IDs: " + invalidIDs);
        }
    }

    static BigDecimal[] solve(List<Point> points) {
        int k = points.size();
        BigDecimal[][] mat = new BigDecimal[k][k + 1];

        for (int i = 0; i < k; i++) {
            BigDecimal x = new BigDecimal(points.get(i).x);
            BigDecimal y = new BigDecimal(points.get(i).y);
            for (int j = 0; j < k; j++) {
                mat[i][j] = x.pow(j);
            }
            mat[i][k] = y;
        }

        for (int i = 0; i < k; i++) {
            BigDecimal div = mat[i][i];
            for (int j = 0; j <= k; j++) {
                mat[i][j] = mat[i][j].divide(div, 50, BigDecimal.ROUND_HALF_UP);
            }

            for (int l = 0; l < k; l++) {
                if (l != i) {
                    BigDecimal factor = mat[l][i];
                    for (int j = 0; j <= k; j++) {
                        mat[l][j] = mat[l][j].subtract(factor.multiply(mat[i][j]));
                    }
                }
            }
        }
        BigDecimal[] ans = new BigDecimal[k];
        for (int i = 0; i < k; i++) {
            ans[i] = mat[i][k];
        }

        return ans;
    }

    static List<List<Integer>> comb(List<Integer> input, int k) {
        List<List<Integer>> ans = new ArrayList<>();
        combHelper(input, 0, k, new ArrayList<>(), ans);
        return ans;
    }

    static void combHelper(List<Integer> input, int start, int k, List<Integer> temp, List<List<Integer>> result) {
        if (temp.size() == k) {
            result.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < input.size(); i++) {
            temp.add(input.get(i));
            combHelper(input, i + 1, k, temp, result);
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
