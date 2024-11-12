package asst2_madhokaa;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;
import java.io.BufferedReader;

public class asst2_madhokaa {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SteepestDescentOptimizer SDO = new SteepestDescentOptimizer();
        
        // Start the program by calling the main method of the optimizer
        SDO.handleInputAndOutput(scanner);
        
        // Close the scanner at the end to prevent resource leaks
        scanner.close();
    }
}

class SteepestDescentOptimizer {
	

    private StringBuilder optimizationLog = new StringBuilder(); // Logs all results for output

    //Menu prompts
	public void handleInputAndOutput(Scanner scanner) {
	    // Step 1: Prompt to enter the program
	    int enterProgram = -1;
	    while (enterProgram != 0 && enterProgram != 1) {
	        System.out.println("Press 0 to exit or 1 to enter the program:");
	        if (scanner.hasNextInt()) {
	            enterProgram = scanner.nextInt();
	            if (enterProgram == 0) {
	                System.out.println("Exiting program...");
	                return;
	            } else if (enterProgram != 1) {
	                System.out.println("Please enter a valid input (0 or 1).");
	            }
	        } else {
	            System.out.println("Please enter a valid input (0 or 1).");
	            scanner.next(); // Clear invalid input
	        }
	    }

	    // Step 2: Input type selection
	    int inputType = -1;
	    while (inputType != 0 && inputType != 1) {
	        System.out.println("Press 0 for .txt input or 1 for manual input:");
	        if (scanner.hasNextInt()) {
	            inputType = scanner.nextInt();
	            if (inputType != 0 && inputType != 1) {
	                System.out.println("Invalid input. Please enter 0 or 1.");
	            }
	        } else {
	            System.out.println("Invalid input. Please enter 0 or 1.");
	            scanner.next(); // Clear invalid input
	        }
	    }

	    // Step 3: Output type selection
	    int outputType = -1;
	    while (outputType != 0 && outputType != 1) {
	        System.out.println("Press 0 for .txt output or 1 for console output:");
	        if (scanner.hasNextInt()) {
	            outputType = scanner.nextInt();
	            if (outputType != 0 && outputType != 1) {
	                System.out.println("Invalid input. Please enter 0 or 1.");
	            }
	        } else {
	            System.out.println("Invalid input. Please enter 0 or 1.");
	            scanner.next(); // Clear invalid input
	        }
	    }

	    // Step 4: Handle paths and processing based on input and output types
	    if (inputType == 0) {
	        // If .txt input, prompt for config file path
	        getFileInput(scanner);
	    } else {
	        // If manual input, collect user data manually
	        getManualInput(scanner);
	    }

	    if (outputType == 0) {
	        // If .txt output, prompt for output file path
	        getFileOutput(scanner);
	    } else {
	        // If console output, display results to console
	        getConsoleOutput();
	    }
	}
	
	// Method to check if each variable is within specified bounds
	private boolean checkBounds(double[] variables, double[] bounds) {
	    for (int i = 0; i < variables.length; i++) {
	        if (variables[i] < bounds[0] || variables[i] > bounds[1]) {
	            System.out.println("Error: Initial point " + variables[i] + " is outside the bounds [" + bounds[0] + ", " + bounds[1] + "].");
	            return false;
	        }
	    }
	    return true;
	}

	public void getManualInput(Scanner scanner) {
	    System.out.println("Enter the choice of objective function (quadratic or rosenbrock):");
	    String functionChoice = scanner.next().toLowerCase();
	    System.out.println("Enter the dimensionality of the problem:");
	    int dimensionality = scanner.nextInt();
	    System.out.println("Enter the number of iterations:");
	    int iterations = scanner.nextInt();
	    System.out.println("Enter the tolerance:");
	    double tolerance = scanner.nextDouble();
	    System.out.println("Enter the step size:");
	    double stepSize = scanner.nextDouble();

	    if (!functionChoice.equals("quadratic") && !functionChoice.equals("rosenbrock") && !functionChoice.equals("rosenbrock_bonus")) {
	        System.out.println("Error: Unknown objective function.");
	        System.exit(0);
	    }

	    System.out.println("Enter the initial point as " + dimensionality + " space-separated values:");
	    scanner.nextLine(); // Clear newline left by nextDouble()
	    String[] pointValues = scanner.nextLine().trim().split(" ");

	    if (pointValues.length != dimensionality) {
	        System.out.println("Error: Initial point dimensionality mismatch.");
	        System.exit(0);
	    }

	    double[] initialPoint = new double[dimensionality];
	    for (int i = 0; i < dimensionality; i++) {
	        initialPoint[i] = Double.parseDouble(pointValues[i]);
	    }

	    // Check bounds for the initial point
	    double[] bounds = {-5.0, 5.0};
	    if (!checkBounds(initialPoint, bounds)) {
	    	System.exit(0); // Exit if bounds are not met
	    }

	    // Instantiate the appropriate function and run optimization
	    ObjectiveFunction objectiveFunction = null;
	    if (functionChoice.equals("quadratic")) {
	        objectiveFunction = new QuadraticFunction();
	    } else if (functionChoice.equals("rosenbrock")) {
	        objectiveFunction = new RosenbrockFunction();
	    } else {
	        System.out.println("Error: Unknown objective function.");
	        System.exit(0);
	    }

	    optimizeSteepestDescent(objectiveFunction, initialPoint, iterations, tolerance, stepSize, dimensionality);
	}


	public void getFileInput(Scanner scanner) {
	    System.out.println("Please provide the path to the config file:");
	    String filePath = scanner.next();

	    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	        String functionChoice = readValue(reader).toLowerCase();
	        int dimensionality = Integer.parseInt(readValue(reader));
	        int iterations = Integer.parseInt(readValue(reader));
	        double tolerance = Double.parseDouble(readValue(reader));
	        double stepSize = Double.parseDouble(readValue(reader));
	        
	        ObjectiveFunction objectiveFunction = null;
	        if ("quadratic".equals(functionChoice)) {
	            objectiveFunction = new QuadraticFunction();
	        } else if ("rosenbrock".equals(functionChoice)) {
	            objectiveFunction = new RosenbrockFunction();
	        } else {
	            System.out.println("Error: Unknown objective function.");
	            System.exit(0);
	        }
	        
	        // Parse the initial point values
	        String[] pointValues = readValue(reader).split(" ");
	        if (pointValues.length != dimensionality) {
	            System.out.println("Error: Initial point dimensionality mismatch.");
	            System.exit(0);
	        }

	        double[] initialPoint = new double[dimensionality];
	        for (int i = 0; i < dimensionality; i++) {
	            initialPoint[i] = Double.parseDouble(pointValues[i]);
	        }

	        // Check bounds for the initial point
	        double[] bounds = {-5.0, 5.0};
	        if (!checkBounds(initialPoint, bounds)) {
	            System.exit(0); // Exit if bounds are not met
	        }

	        // Instantiate the appropriate function and run optimization
	       
	        optimizeSteepestDescent(objectiveFunction, initialPoint, iterations, tolerance, stepSize, dimensionality);

	    } catch (IOException | NumberFormatException e) {
	        System.out.println("Error reading the file.");
	        System.exit(0);
	    }
	}

	// Helper method to read a line and strip comments
	private String readValue(BufferedReader reader) throws IOException {
	    String line = reader.readLine();
	    if (line != null) {
	        line = line.split("//")[0].trim();
	    }
	    return line;
	}


    // Handle file-based output
    public void getFileOutput(Scanner scanner) {
        System.out.println("Please provide the path for the output file:");
        String outputPath = scanner.next();
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(optimizationLog.toString());
            //System.out.println("Results successfully written to " + outputPath);
        } catch (IOException e) {
            System.out.println("Error: Could not write to file " + outputPath);
        }
    }

    // Handle console-based output
    public void getConsoleOutput() {
    	System.out.println(optimizationLog.toString());
    }

    public void optimizeSteepestDescent(ObjectiveFunction objectiveFunction, double[] variables, int iterations, double tolerance, double stepSize, int dimensionality) { 
    	
        optimizationLog.append("Objective Function: ").append(objectiveFunction.getName()).append("\n")
                       .append("Dimensionality: ").append(dimensionality).append("\n")
                       .append("Initial Point: ");
        
        for (int i = 0; i < variables.length; i++) {
            optimizationLog.append(variables[i]);
            if (i < variables.length - 1) {
                optimizationLog.append(" "); // Add a space between values
            }
        }
        optimizationLog.append("\nIterations: ").append(iterations).append("\n")
                       .append("Tolerance: ").append(String.format("%.5f", tolerance)).append("\n")
                       .append("Step Size: ").append(String.format("%.5f", stepSize)).append("\n\n")
                       .append("Optimization process:\n");	

        double previousGradientMagnitude = 0;
        
        for (int iter = 0; iter < iterations; iter++) {
            double objectiveValue = objectiveFunction.compute(variables);
            
            optimizationLog.append("Iteration ").append(iter + 1).append(":\n")
                           .append("Objective Function Value: ").append(String.format("%.5f", objectiveValue)).append("\n")
                           .append("x-values: ");
            
            // Append rounded x-values in line
            for (double variable : variables) {
                optimizationLog.append(String.format("%.5f ", variable));
            }
            optimizationLog.append("\n");

            double[] gradient = objectiveFunction.computeGradient(variables);
            double gradientMagnitude = computeMagnitude(gradient);

            // Print Current Tolerance if it's not the first iteration
            if (iter > 0) {
                optimizationLog.append("Current Tolerance: ").append(String.format("%.5f", previousGradientMagnitude)).append("\n");
                if (previousGradientMagnitude < tolerance) {
                    optimizationLog.append("\nConvergence reached after ").append(iter + 1).append(" iterations.\n\n");
                    break;
                }
            }

            previousGradientMagnitude = gradientMagnitude;
            
            // Update variables with step size and gradient
            for (int i = 0; i < dimensionality; i++) {
                variables[i] -= stepSize * gradient[i];
                variables[i] = floorTo5Decimals(variables[i]);
            }

            optimizationLog.append("\n");
        }

        // Final check if max iterations were reached
        if (previousGradientMagnitude >= tolerance) {
            optimizationLog.append("Maximum iterations reached without satisfying the tolerance.\n\n");
        }
        optimizationLog.append("Optimization process completed.\n");
    }

    private double floorTo5Decimals(double value) {
        BigDecimal bd = new BigDecimal(value).setScale(5, RoundingMode.FLOOR);
        return bd.doubleValue();
    }

	private double computeMagnitude(double[] gradient) {
        double gradientMagnitude = 0;
        for (double grad : gradient) {
            gradientMagnitude += grad * grad;
        }
        return floorTo5Decimals(Math.sqrt(gradientMagnitude));
    }

abstract class ObjectiveFunction {

    public abstract double compute(double[] variables);
    public abstract double[] computeGradient(double[] variables);
    public abstract double[] getBounds();
    public abstract String getName();
}

class QuadraticFunction extends ObjectiveFunction {
    @Override
    public double compute(double[] variables) {
        double ofv = 0;
        for (double num : variables) {
            ofv += (num * num);
        }
        return floorTo5Decimals(ofv);
    }

    public double[] computeGradient(double[] variables) {
        double[] grad = new double[variables.length];
        for (int i = 0; i < variables.length; i++) {
            grad[i] = 2 * variables[i];
        }
        return grad;
    }

    public double[] getBounds() {
        double[] bounds = {-5, 5};
        return bounds;
    }

    public String getName() {
        String name = "Quadratic";
        return name;
    }
}

class RosenbrockFunction extends ObjectiveFunction {
    @Override
    public double compute(double[] variables) {
        double ofv = 0;
        for (int i = 0; i < (variables.length) - 1; i++) {
            ofv += 100 * Math.pow((variables[i + 1] - (variables[i] * variables[i])), 2) + Math.pow(1 - variables[i], 2);
        }
        return floorTo5Decimals(ofv);
    }

    public double[] computeGradient(double[] variables) {
        int d = variables.length;
        double[] grad = new double[d];
        for (int i = 0; i < d - 1; i++) {
            grad[i] = -400 * variables[i] * (variables[i + 1] - variables[i] * variables[i]) - 2 * (1 - variables[i]);
        }

        for (int i = 1; i < d; i++) {
            grad[i] = 200 * (variables[i] - variables[i - 1] * variables[i - 1]);
        }

        return grad;
    }

    public double[] getBounds() {
        double[] bounds = {-5, 5};
        return bounds;
    }

    public String getName() {
        String name = "Rosenbrock";
        return name;
    }
}
}
