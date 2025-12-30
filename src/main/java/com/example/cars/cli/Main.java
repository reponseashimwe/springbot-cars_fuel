package com.example.cars.cli;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String command = args[0];
        String[] commandArgs = extractCommandArgs(args);

        switch (command) {
            case "list-cars":
                CarCLI.listCars();
                break;
            case "get-car":
                CarCLI.getCar(commandArgs);
                break;
            case "create-car":
                CarCLI.createCar(commandArgs);
                break;
            case "update-car":
                CarCLI.updateCar(commandArgs);
                break;
            case "delete-car":
                CarCLI.deleteCar(commandArgs);
                break;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
        }
    }

    /**
     * Extracts command arguments (everything after the command itself).
     */
    private static String[] extractCommandArgs(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        String[] commandArgs = new String[args.length - 1];
        System.arraycopy(args, 1, commandArgs, 0, args.length - 1);
        return commandArgs;
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar cars-cli.jar <command> [arguments]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  list-cars");
        System.out.println("  get-car --carId <id>");
        System.out.println("  create-car --brand <brand> --model <model> --year <year>");
        System.out.println("  update-car --carId <id> --brand <brand> --model <model> --year <year>");
        System.out.println("  delete-car --carId <id>");
    }
}
