package Main;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.fitting.HarmonicCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.transform.FastFourierTransformer;
import Plot.PlotWindow;
import Plot.ScatterPlot;

public class ReadStepsData implements StepCounter {
	public static void main(String[] args) {
		ReadStepsData bruh = new ReadStepsData();

		bruh.countSteps(Utils.readFile("testFiles/blk3/Walking 300 Steps Constant Speed Data - itai.csv"));

	}

	public ReadStepsData() {

	}

	public int countSteps(ArrayList<Double> xAcc, ArrayList<Double> yAcc, ArrayList<Double> zAcc,
			ArrayList<Double> xGyro, ArrayList<Double> yGyro, ArrayList<Double> zGyro) {

		int threshold = 0;
		List<Double> magnitudes = Utils.getMagnitudes(xAcc, yAcc, zAcc);

		// magnitudes = Utils.applyBasicMedianFilter(magnitudes);

		for (int i = 0; i < 1600; i++) {
			Utils.applyWeightedAverage(1/6.0, 2/3.0, 1/6.0, magnitudes);
		}
		
		// plotData(magnitudes, threshold);

		System.out.println(calculateSteps(magnitudes, threshold));

		return calculateSteps(magnitudes, threshold);
	}

	public int countSteps(String csvFileText) {
		String[] lines = csvFileText.split("\n");
		String[][] data = new String[lines.length][];
		for (int i = 0; i < lines.length; i++) {
			data[i] = lines[i].split(",");
		}

		ArrayList<Double> xAcc = new ArrayList<Double>();
		ArrayList<Double> yAcc = new ArrayList<Double>();
		ArrayList<Double> zAcc = new ArrayList<Double>();

		for (int i = 1; i < data.length; i++) {
			xAcc.add(Double.parseDouble(data[i][0]));
			yAcc.add(Double.parseDouble(data[i][1]));
			zAcc.add(Double.parseDouble(data[i][2]));
		}

		return countSteps(xAcc, yAcc, zAcc, null, null, null);
	}

	public static void processData(String filePath, double threshold) {
		String ouputPath = Utils.getOutputPath(filePath);
		String outputStr = "Threshold: " + threshold + "\n";

		HashMap<String, List<String>> dataAsStrings = Utils.readCSVFileAsMap(filePath);

		HashMap<String, List<Double>> data = Utils.parseCSVString(dataAsStrings);

		List<Double> magnitudes = Utils.getMagnitudes(data.get("lsm6dsr_accelerometer.x"),
				data.get("lsm6dsr_accelerometer.y"),
				data.get("lsm6dsr_accelerometer.z"));

		outputStr += "Length of data before filtering: " + magnitudes.size() + "\n";

		// magnitudes = Utils.applyBasicMedianFilter(magnitudes);
		outputStr += "Length of data after median filter: " + magnitudes.size() +
				"\n";

		magnitudes = Utils.applyMovingAverage(magnitudes, 10);
		outputStr += "Number of steps after moving average: " + calculateSteps(magnitudes, threshold) + "\n";

		plotData(magnitudes, threshold);

		magnitudes = Utils.applyTheCurve(magnitudes, 0.955);
		outputStr += "Number of steps after the curve: " + magnitudes.size() + "\n";

		// magnitudes = Utils.applyClosePeakFilter(magnitudes, 5);
		outputStr += "Number of steps after close peak filter: " + calculateSteps(magnitudes, threshold) + "\n";

		Utils.writeToFile(ouputPath, outputStr);

		plotData(magnitudes, threshold);

	}

	private static int calculateSteps(List<Double> magnitudes, double threshold) {
		int stepCount = 0;
		for (int i = 1; i < magnitudes.size() - 1; i++) {
			if (isPeak(magnitudes, i, threshold) || isValley(magnitudes, i, threshold)) {
				stepCount++;
			}
		}

		return stepCount;
	}

	private static void plotData(List<Double> data, double threshold) {
		ScatterPlot plt = new ScatterPlot(100, 100, 1100, 700);
		WeightedObservedPoints obs = new WeightedObservedPoints();

		for (int i = 0; i < data.size(); i++) {
			plt.plot(0, i, data.get(i)).strokeColor("red").strokeWeight(2).style("-");
			obs.add(i, data.get(i));

			if (isPeak(data, i, threshold) || isValley(data, i, threshold)) {
				plt.plot(1, i, data.get(i)).strokeColor("green").strokeWeight(2).style(".");
			}
		}

		HarmonicCurveFitter fitter = HarmonicCurveFitter.create();
		double[] params = fitter.fit(obs.toList());
		// Print the equation for the fitted curve
		System.out.println(params[0] + " * sin(" + params[1] + " * x + " + params[2] + ")");

		for (int i = 0; i < data.size(); i++) {
			plt.plot(2, i, params[0] * Math.sin(params[1] * i + params[2]) + 10).strokeColor("blue").strokeWeight(2)
					.style("-");
		}

		PlotWindow window = PlotWindow.getWindowFor(plt, 1200, 800);
		window.show();
	}

	static boolean isPeak(List<Double> magnitudes, int index, double threshold) {
		try {
			return magnitudes.get(index) - magnitudes.get(index - 1) > threshold
					&& magnitudes.get(index) - magnitudes.get(index + 1) > threshold;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	static boolean isValley(List<Double> magnitudes, int index, double threshold) {
		try {
			return magnitudes.get(index - 1) - magnitudes.get(index) > threshold
					&& magnitudes.get(index + 1) - magnitudes.get(index) > threshold;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

}