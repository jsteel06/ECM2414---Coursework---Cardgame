import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//shared helpers used by the test classes, not a test class itself
public class TestHelper {

    // no constructor
    private TestHelper() {
    }

    //makes a list of cards from values
    static List<Card> list(int... values) {
        List<Card> cards = new ArrayList<>();
        for (int value : values) {
            cards.add(new Card(value));
        }
        return cards;
    }

    //turns cards back into values so lists can be compared with assertEquals
    static List<Integer> values(List<Card> cards) {
        List<Integer> list = new ArrayList<>();
        for (Card card : cards) {
            list.add(card.getValue());
        }
        return list;
    }

    //writes the lines exactly as given, so tests can make broken pack files
    static File writePackFile(File folder, String name, String... lines) throws IOException {
        File file = new File(folder, name);
        Files.write(file.toPath(), List.of(lines));
        return file;
    }

    //reads every line of an output file in the current folder
    static List<String> readLines(String fileName) throws IOException {
        return Files.readAllLines(Path.of(fileName));
    }

    //takes a hand or deck output string and convert card values into an array
    //e.g. "player 1 hand: 2 2 3 5" goes to [2, 2, 3, 5]
    static List<Integer> valuesFromString(String line) {
        List<Integer> list = new ArrayList<>();
        String afterColon = line.substring(line.indexOf(':') + 1).trim();
        //if theres no cards.
        if (afterColon.isEmpty()) {
            return list;
        }
        for (String part : afterColon.split(" ")) {
            list.add(Integer.parseInt(part));
        }
        return list;
    }

    //deletes player and deck output files 1 to n so every test starts clean
    static void deleteOutputFiles(int n) {
        for (int i = 1; i <= n; i++) {
            new File("player" + i + "_output.txt").delete();
            new File("deck" + i + "_output.txt").delete();
        }
    }
}
