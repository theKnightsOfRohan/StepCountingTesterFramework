package Testing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import Main.ReadStepsData;
import Main.StepCounter;

public class MainTester {
    private static final String TEST_FILE_FOLDER = "testFiles/blk3";

    public static void main(String[] args) {
        StepCounter counter = new ReadStepsData();

        ArrayList<Path> paths = getPaths();

        System.out.println("Filename \t\t\t prediction \t\t correct \t\t error");
        double totalError = 0;
        int count = 0;
        for (Path path : paths) {
            FileData data = processPath(path);
            
            int prediction = counter.countSteps(data.text);
            count++;

            int error = data.correctNumberOfSteps - prediction;
            totalError += (error * error);
            System.out
                    .println(data.filePath + "\t\t" + prediction + "\t\t" + data.correctNumberOfSteps + "\t\t" + error);
        }
        System.out.println();
        System.out.println("Mean squared error: " + (totalError / count));

    }

    private static FileData processPath(Path path) {
        String filename = path.getFileName().toString();
        int numSteps = extractNumSteps(path);
        String text;

        if (numSteps == -1) {
            System.err.println("Couldn't get correct # of steps for file: " + path);
            return null;
        }

        try {
            text = readFile(path.toString());
        } catch (Exception e) {
            System.err.println("Error reading the file: " + path);
            return null;
        }

        return new FileData(text, path.toString(), numSteps);
    }

    public static int extractNumSteps(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        if (filename.contains("step")) {
            filename = filename.substring(0, filename.indexOf("step"));
        }
        filename = filename.replaceAll("[^\\d]", "");
        int steps;
        try {
            steps = Integer.parseInt(filename.trim());
        } catch (Exception e) {
            System.err.println("Error extracting # of steps from filename: " + filename);
            return -1;
        }

        return steps;
    }

    private static ArrayList<Path> getPaths() {
        ArrayList<Path> paths = new ArrayList<>();
        Path workDir = Paths.get(TEST_FILE_FOLDER);
        if (!Files.notExists(workDir)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(workDir)) {
                for (Path p : directoryStream) {
                    paths.add(p);
                }
                return paths;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static String readFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }
}
