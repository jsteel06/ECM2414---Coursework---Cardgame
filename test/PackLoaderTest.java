import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

//PackLoader should accept exactly 8n non-negative integers and reject anything else
public class PackLoaderTest {

    //a fresh empty folder for each test, deleted afterwards, so pack files don't pile up
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    //valid packs

    //cards come back in file order, index 0 is the top of the pack
    @Test
    public void testValidPackLoadsInFileOrder() throws Exception {
        // TODO: write "1".."8", load with n = 1, check values are 1..8 in order
        fail("not written yet");
    }

    //a pack for 4 players needs exactly 32 cards
    @Test
    public void testValidPackForFourPlayers() throws Exception {
        // TODO: write 32 lines, load with n = 4, check size is 32
        fail("not written yet");
    }

    //0 is a valid card value
    @Test
    public void testZeroIsAValidCard() throws Exception {
        // TODO: pack containing a 0 loads without an exception
        fail("not written yet");
    }

    //spaces either side of a number are ignored
    @Test
    public void testSpacesAroundNumbersAreIgnored() throws Exception {
        // TODO: lines like " 5 " load as 5
        fail("not written yet");
    }

    //invalid packs, each should throw InvalidPackException

    @Test(expected = PackLoader.InvalidPackException.class)
    public void testTooFewLinesRejected() throws Exception {
        // TODO: 7 lines with n = 1
        fail("not written yet");
    }

    @Test(expected = PackLoader.InvalidPackException.class)
    public void testTooManyLinesRejected() throws Exception {
        // TODO: 9 lines with n = 1
        fail("not written yet");
    }

    @Test(expected = PackLoader.InvalidPackException.class)
    public void testEmptyFileRejected() throws Exception {
        // TODO: file with no lines
        fail("not written yet");
    }

    @Test(expected = PackLoader.InvalidPackException.class)
    public void testBlankLineRejected() throws Exception {
        // TODO: 8 lines where one is ""
        fail("not written yet");
    }

    @Test(expected = PackLoader.InvalidPackException.class)
    public void testNegativeNumberRejected() throws Exception {
        // TODO: 8 lines where one is "-2"
        fail("not written yet");
    }

    @Test(expected = PackLoader.InvalidPackException.class)
    public void testNonIntegerRejected() throws Exception {
        // TODO: 8 lines where one is "abc"
        fail("not written yet");
    }

    @Test(expected = PackLoader.InvalidPackException.class)
    public void testDecimalRejected() throws Exception {
        // TODO: 8 lines where one is "2.5"
        fail("not written yet");
    }

    @Test(expected = PackLoader.InvalidPackException.class)
    public void testTwoNumbersOnOneLineRejected() throws Exception {
        // TODO: 8 lines where one is "2 3"
        fail("not written yet");
    }

    @Test(expected = PackLoader.InvalidPackException.class)
    public void testMissingFileRejected() throws Exception {
        // TODO: load a path inside folder that was never created
        fail("not written yet");
    }

    @Test(expected = PackLoader.InvalidPackException.class)
    public void testFolderInsteadOfFileRejected() throws Exception {
        // TODO: load folder.getRoot().getPath()
        fail("not written yet");
    }

    //n must be at least 1, this is a programming mistake not a bad file
    @Test(expected = IllegalArgumentException.class)
    public void testZeroPlayersRejected() throws Exception {
        // TODO: load a valid file with n = 0
        fail("not written yet");
    }

    //the user is told which line is wrong
    @Test
    public void testErrorMessageNamesTheBadLine() throws Exception {
        // TODO: line 2 is "abc", catch InvalidPackException, assertTrue(e.getMessage().contains("line 2"))
        fail("not written yet");
    }
}
