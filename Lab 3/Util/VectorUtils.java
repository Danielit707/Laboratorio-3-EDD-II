package util;

import java.util.concurrent.ThreadLocalRandom;

public class VectorUtils {
    public static int[] generateRandomVector(int n, int maxExponent) {
        
        int maxValue = (int) Math.pow(2, maxExponent);

        int[] vector = new int[n];

        for (int i = 0; i < n; i++) {
            // Generar un valor entre el valor mínimo (-2^30) y el máximo (2^30)
            int value = ThreadLocalRandom.current().nextInt(-maxValue, maxValue + 1);
            vector[i] = value;
        }

        return vector;
    }
}


