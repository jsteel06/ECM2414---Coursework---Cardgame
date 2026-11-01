import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//reads a pack file and validates for 8n lines and all non negative intergers
//public for testing
public class PackLoader {

    //thrown when the pack cant be used
    //the message says what is wrong so CardGame can output  to the user and ask again
    public static class InvalidPackException extends Exception {
        public InvalidPackException(String message) {
            super(message);
        }
    }

    // no constructor
    private PackLoader() {
    }

    //reads the pack file and returns its cards
    public static List<Card> load(String path, int n) throws InvalidPackException {
        if (n < 1) {
            throw new IllegalArgumentException("number of players must be positive");
        }

        //check file can be read
        File packFile = new File(path);
        if (!packFile.isFile() || !packFile.canRead()) {
            throw new InvalidPackException("'" + path + "' was not found or could not be read");
        }

        List<Card> pack = new ArrayList<>();

        //reads file as utf-8
        try (BufferedReader reader = new BufferedReader(new FileReader(packFile, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++; //increases line count
                pack.add(parseCard(line, lineNumber)); //adds card to pack
            }
        } catch (IOException e) {
            throw new InvalidPackException(e.getMessage());
        }

        //long so a huge n wont overflow
        long expected = 8L * n;
        if (pack.size() != expected) {
            throw new InvalidPackException("expected " + expected + " cards but found " + pack.size());
        }
        return pack;
    }

    //turns one line of the file into a card
    private static Card parseCard(String line, int lineNumber) throws InvalidPackException {

        //remove spaces either side
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidPackException("line " + lineNumber + " is blank");
        }

        int value;
        try {
            value = Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            throw new InvalidPackException("line " + lineNumber + " is not an integer");
        }

        if (value < 0) {
            throw new InvalidPackException("line " + lineNumber + " is negative");
        }
        return new Card(value);
    }
}
