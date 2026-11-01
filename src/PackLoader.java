import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//reads a pack file and checks it is valid before the game starts
//a valid pack has exactly 8n lines, each holding a positive integer
//public so the validation can be unit tested
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
        List<Card> pack = new ArrayList<>();
        // TODO:
        //  1. File packFile = new File(path)
        //     if cant be read throw InvalidPackException
        //  2. open it with a BufferedReader
        //  3. read line by line, counting line numbers (start at 1) for error messages
        //  4. catch IOException and rethrow it as InvalidPackException
        //  5. use 8L * n (long) so a huge n can't overflow
        //     if pack.size() != 8n, throw e.g. "expected 32 cards but found 30"
        return pack;
    }

    //turns one line of the file into a card
    private static Card parseCard(String line, int lineNumber) throws InvalidPackException {
        // TODO:
        //  - trim spaces off the line
        //  - if it's empty, throw "line x is blank"
        //  - Integer.parseInt the line
        //      catch NumberFormatException and throw "line x is not a whole number"
        //  - if value < 0 throw "line x is negative"
        //  - otherwise return new Card(value)
        return null;
    }
}
