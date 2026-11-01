import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

//writes a random valid pack file
public class PackGenerator {

    //no constructor
    private PackGenerator() {
    }

    //main method
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        //value inputs
        int n = askForInt(input, "Please enter the number of players:", null);
        int maxValue = askForInt(input, "Please enter the value of the highest card, default is" + n, n);

        //file name input
        System.out.println("Please enter file name, default is pack"+ n + ".txt):");
        String name = input.nextLine().trim();

        //logic for default file name
        if (name.isEmpty()) {
            name = "pack" + n + ".txt";
        }


        File file = new File(name);
        try {
            //calls writePack and generate methods to generate file
            writePack(generate(n, maxValue, new Random()), file);
            System.out.println("File generated");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    //keeps asking until the user types a positive whole number
    private static int askForInt(Scanner input, String prompt, Integer defaultValue) {
        while (true) {

            System.out.println(prompt);
            //gets input
            String line = input.nextLine().trim();
            //uses default value when no input and defult value isnt null
            if (line.isEmpty() && defaultValue != null) {
                return defaultValue;
            }

            //gets input and checks whether its a positive interger
            try {
                int value = Integer.parseInt(line);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // not a whole number, fall through to the error message below
            }
            System.out.println("Invalid input: please enter a positive integer.");
        }
    }


        //returns 8n random card values
    public static List<Integer> generate(int n, int maxValue, Random random) {
        if (n < 1) {
            throw new IllegalArgumentException("number of players must be positive");
        }
        if (maxValue < 1) {
            throw new IllegalArgumentException("highest card value must be at least 1");
        }
        if (8L * n > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("too many players");
        }

        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < 8 * n; i++) {
            values.add(1 + random.nextInt(maxValue));
        }
        return values;
    }

    //writes the values to the file
    //public for tests
    public static void writePack(List<Integer> values, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int value : values) {
                writer.write(String.valueOf(value));
                writer.newLine();
            }
        }
    }
}
