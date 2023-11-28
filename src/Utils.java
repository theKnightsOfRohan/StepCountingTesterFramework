
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

public class Utils {
	public static HashMap<String, List<Double>> parseCSVString(HashMap<String, List<String>> dataAsStrings) {
		HashMap<String, List<Double>> data = new HashMap<String, List<Double>>();
		for (String key : dataAsStrings.keySet()) {
			data.put(key, new ArrayList<Double>());
			for (String value : dataAsStrings.get(key)) {
				data.get(key).add(Double.parseDouble(value));
			}
		}
		return data;
	}

	public static HashMap<String, List<String>> readCSVFileAsMap(String filePath) {
		HashMap<String, List<String>> data = new HashMap<String, List<String>>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String[] lineNames = reader.readLine().split(",");
			for (String name : lineNames) {
				data.put(name, new ArrayList<String>());
			}
			String line = reader.readLine();
			while (line != null) {
				String[] row = line.split(",");
				for (int i = 0; i < row.length; i++) {
					data.get(lineNames[i]).add(row[i]);
				}
				line = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

	public static List<Double> getMagnitudes(List<Double> x, List<Double> y, List<Double> z) {
		List<Double> magnitudes = new ArrayList<Double>();
		for (int i = 0; i < x.size(); i++) {
			magnitudes.add(Math.sqrt(Math.pow(x.get(i), 2) + Math.pow(y.get(i), 2) + Math.pow(z.get(i), 2)));
		}
		return magnitudes;
	}

	public static void writeToCSVFile(String filePath, List<Double> data) {
		try (java.io.PrintWriter writer = new java.io.PrintWriter(filePath)) {
			for (int i = 0; i < data.size(); i++) {
				writer.println(i + "," + data.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<Double> applyBasicMedianFilter(List<Double> magnitudes) {
		List<Double> filteredMagnitudes = new ArrayList<Double>();

		double medianPeakDifference = getMedianPeakDifference(magnitudes);

		for (int i = 0; i < magnitudes.size(); i++) {
			if (i == 0 || i == magnitudes.size() - 1) {
				filteredMagnitudes.add(magnitudes.get(i));
			} else if (ReadStepsData.isPeak(magnitudes, i, medianPeakDifference)
					|| ReadStepsData.isValley(magnitudes, i, medianPeakDifference)) {
				filteredMagnitudes.add(magnitudes.get(i));
			} else {
				filteredMagnitudes.add((magnitudes.get(i - 1) + magnitudes.get(i) + magnitudes.get(i + 1)) / 3);
			}
		}

		return filteredMagnitudes;
	}

	private static double getMedianPeakDifference(List<Double> magnitudes) {
		List<Double> peakDifferences = new ArrayList<Double>();
		for (int i = 1; i < magnitudes.size() - 1; i++) {
			if (ReadStepsData.isPeak(magnitudes, i, 0) || ReadStepsData.isValley(magnitudes, i, 0)) {
				peakDifferences.add(Math.abs(magnitudes.get(i) - magnitudes.get(i - 1)));
			}
		}
		return getMedian(peakDifferences);
	}

	private static double getMedian(List<Double> peakDifferences) {
		peakDifferences.sort((a, b) -> a.compareTo(b));
		return peakDifferences.get(peakDifferences.size() / 2);
	}

	public static List<Double> applyMovingAverage(List<Double> magnitudes, int spread) {
		List<Double> filteredMagnitudes = new ArrayList<Double>();
		for (int i = 0; i < magnitudes.size(); i++) {
			double sum = 0;
			for (int j = i - spread; j < i + spread; j++) {
				if (j >= 0 && j < magnitudes.size()) {
					sum += magnitudes.get(j);
				}
			}
			filteredMagnitudes.add(sum / (spread * 2 + 1));
		}
		return filteredMagnitudes;
	}

    public static List<Double> applyClosePeakFilter(List<Double> magnitudes, int spacing) {
        List<Double> filteredMagnitudes = new ArrayList<Double>();
		List<Integer> IndexOfpeaksAndValleys = getIndexOfPeaksAndValleys(magnitudes);

		for (int i = 0; i < IndexOfpeaksAndValleys.size()-1; i++) {
			// Average between points. 
			if (IndexOfpeaksAndValleys.get(i) - IndexOfpeaksAndValleys.get(i+1) <= spacing) {
				filteredMagnitudes.add(magnitudes.get(IndexOfpeaksAndValleys.get(i)));
				i++;
				continue;
			}
			filteredMagnitudes.add(magnitudes.get(IndexOfpeaksAndValleys.get(i)));
		}

		return filteredMagnitudes;
    }

	private static List<Integer> getIndexOfPeaksAndValleys(List<Double> magnitudes) {
		List<Integer> returnList = new ArrayList<Integer>();

		for (int i = 1; i < magnitudes.size()-1; i++) {
			if (ReadStepsData.isPeak(magnitudes, i, 0) || ReadStepsData.isValley(magnitudes, i, 0)) {
				returnList.add(i);
			}
		}

		return returnList;
	}

	public static String getOutputPath(String filePath) {
		List<String> pathParts = Arrays.asList(filePath.split("/"));
		pathParts.set(pathParts.size() - 2, "Output");
		String fileName = pathParts.get(pathParts.size() - 1);
		pathParts.set(pathParts.size() - 1, fileName.substring(0, fileName.length() - 4) + "_output.txt");
		return String.join("/", pathParts);
	}

	public static void writeToFile(String filePath, String data) {
		try (PrintWriter writer = new PrintWriter(filePath)) {
			writer.println(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeToFile(String filePath, List<Object> data) {
		try (PrintWriter writer = new PrintWriter(filePath)) {
			for (Object line : data) {
				writer.println(line.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> readFileByLine(String filePath) {
		List<String> lines = new java.util.ArrayList<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line = reader.readLine();
			while (line != null) {
				lines.add(line);
				line = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static String readFile(String filePath) {
		String data = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line = reader.readLine();
			while (line != null) {
				data += line + "\n";
				line = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

    public static List<Double> applyTheCurve(List<Double> magnitudes, double d) {
        List<Double> returnArr = new ArrayList<Double>();
		Double[] test = new Double[magnitudes.size()];
		Double[] toArr = magnitudes.toArray(test);

		Arrays.sort(toArr);

		Double[] test2 = Arrays.copyOfRange(toArr, (int) (d * toArr.length), toArr.length);

		return Arrays.asList(test2);
    }
}
