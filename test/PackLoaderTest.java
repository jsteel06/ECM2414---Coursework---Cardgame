import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

//PackLoader should accept exactly 8n non-negative integers and reject anything else
public class PackLoaderTest {

    //a fresh empty folder for each test, deleted afterwards
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    //makes the lines "1", "2" ... up to count, a valid pack when count is 8n
    private String[] validLines(int count) {
        String[] lines = new String[count];
        for (int i = 0; i < count; i++) {
            lines[i] = String.valueOf(i + 1);
        }
        return lines;
    }

    //8 valid lines plus one bad line, so the pack is only invalid because of the bad line
    //if PackLoader skipped the bad line instead of rejecting it, the other 8 would look like a valid pack
    private String[] validLinesPlus(String badLine) {
        String[] lines = new String[9];
        String[] valid = validLines(8);
        for (int i = 0; i < 8; i++) {
            lines[i] = valid[i];
        }
        lines[8] = badLine;
        return lines;
    }

    //writes the lines into pack.txt in the temporary folder and loads it
    private List<Card> load(String[] lines, int n) throws Exception {
        File file = TestHelper.writePackFile(folder.getRoot(), "pack.txt", lines);
        return PackLoader.load(file.getPath(), n);
    }



    //cards come back in file order, index 0 is the top of the pack
    @Test
    public void testValidPackLoadsInFileOrder() throws Exception {
        List<Card> pack = load(validLines(8), 1);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), TestHelper.values(pack));
    }

    //a pack for 4 players needs exactly 32 cards
    @Test
    public void testValidPackForFourPlayers() throws Exception {
        List<Card> pack = load(validLines(32), 4);
        assertEquals(32, pack.size());
        assertEquals(1, pack.get(0).getValue());
        assertEquals(32, pack.get(31).getValue());
    }

    //0 is a valid card value
    @Test
    public void testZeroIsAValidCard() throws Exception {
        String[] lines = validLines(8);
        lines[0] = "0";
        List<Card> pack = load(lines, 1);
        assertEquals(0, pack.get(0).getValue());
    }

    //spaces and tabs either side of a number are ignored
    @Test
    public void testSpacesAroundNumbersAreIgnored() throws Exception {
        String[] lines = validLines(8);
        lines[0] = " 5 ";
        lines[1] = " 6";
        List<Card> pack = load(lines, 1);
        assertEquals(5, pack.get(0).getValue());
        assertEquals(6, pack.get(1).getValue());
    }

    //invalid packs, each should throw InvalidPackException

    //7 lines is one short of 8
    @Test(expected = PackLoader.InvalidPackException.class)
    public void testTooFewLinesRejected() throws Exception {
        load(validLines(7), 1);
    }

    //9 lines is one too many
    @Test(expected = PackLoader.InvalidPackException.class)
    public void testTooManyLinesRejected() throws Exception {
        load(validLines(9), 1);
    }

    //an empty file has no cards at all
    @Test(expected = PackLoader.InvalidPackException.class)
    public void testEmptyFileRejected() throws Exception {
        load(new String[0], 1);
    }

    //a blank line isnt a card
    @Test(expected = PackLoader.InvalidPackException.class)
    public void testBlankLineRejected() throws Exception {
        load(validLinesPlus(""), 1);
    }

    //negative cards arent allowed
    @Test(expected = PackLoader.InvalidPackException.class)
    public void testNegativeNumberRejected() throws Exception {
        String[] lines = validLines(8);
        lines[1] = "-1";
        load(lines, 1);
    }

    //letters arent an interger
    @Test(expected = PackLoader.InvalidPackException.class)
    public void testNonIntegerRejected() throws Exception {
        load(validLinesPlus("abc"), 1);
    }

    //cards have to be whole numbers
    @Test(expected = PackLoader.InvalidPackException.class)
    public void testDecimalRejected() throws Exception {
        load(validLinesPlus("2.5"), 1);
    }

    //only one card per line
    @Test(expected = PackLoader.InvalidPackException.class)
    public void testTwoNumbersOnOneLineRejected() throws Exception {
        load(validLinesPlus("2 3"), 1);
    }

    //a file that doesnt exist cant be read
    @Test(expected = PackLoader.InvalidPackException.class)
    public void testMissingFileRejected() throws Exception {
        File missing = new File(folder.getRoot(), "missing.txt");
        PackLoader.load(missing.getPath(), 1);
    }


    //n must be at least 1
    @Test(expected = IllegalArgumentException.class)
    public void testZeroPlayersRejected() throws Exception {
        load(validLines(8), 0);
    }



    //the user is told which line is wrong
    @Test
    public void testErrorMessageNamesTheBadLine() throws Exception {
        String[] lines = validLines(8);
        lines[1] = "abc";
        try {
            load(lines, 1);
            fail("an invalid pack was accepted");
        } catch (PackLoader.InvalidPackException e) {
            assertTrue(e.getMessage().contains("line 2"));
        }
    }

    //the user is told how many cards were expected and how many were found
    @Test
    public void testWrongLengthMessageGivesBothCounts() throws Exception {
        try {
            load(validLines(7), 1);
            fail("an invalid pack was accepted");
        } catch (PackLoader.InvalidPackException e) {
            assertTrue(e.getMessage().contains("8"));
            assertTrue(e.getMessage().contains("7"));
        }
    }
}
