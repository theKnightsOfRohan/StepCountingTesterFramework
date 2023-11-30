package Main;

import java.util.ArrayList;
import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        ArrayList<Double> testArr = new ArrayList<Double>();
        testArr.add(3.0);
        testArr.add(6.0);
        testArr.add(7.0);
        testArr.add(5.0);
        testArr.add(2.0);

        applyWeightedAverage(0.25, 0.5, 1/4.0, testArr);

        System.out.println(Arrays.toString(testArr.toArray(new Double[0])));
    }

    public static void applyWeightedAverage (double weight1, double weight2, double weight3, ArrayList<Double> a) {
        for (int i = 1; i < a.size()-1; i++) {
            double w1 = weight1 * a.get(i-1);
            double w2 = weight2 * a.get(i);
            double w3 = weight3 * a.get(i+1);
            a.set(i, (w1 + w2 + w3));
        }
    }
}
