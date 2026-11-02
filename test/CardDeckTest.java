import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

//CardDeck should be a thread-safe first in first out queue
public class CardDeckTest {

    private CardDeck deck;

    @Before
    public void setUp() {
        deck = new CardDeck(1);
    }

    //writeOutput creates deck1_output.txt in the current folder
    @After
    public void tearDown() {
        TestHelper.deleteOutputFiles(2);
    }

    //cards come out in the same order they went in
    @Test
    public void testDrawOrderIsFirstInFirstOut() {
        // TODO: add 1, 2, 3 to the bottom, draw three times, check the values are 1, 2, 3
        fail("not written yet");
    }

    //drawing from an empty deck gives null instead of crashing
    @Test
    public void testDrawFromEmptyDeckReturnsNull() {
        // TODO: assertNull(deck.drawFromTop())
        fail("not written yet");
    }

    //a null card can't be added
    @Test(expected = IllegalArgumentException.class)
    public void testAddingNullCardRejected() {
        // TODO: deck.addToBottom(null)
        fail("not written yet");
    }

    //output line matches the spec, e.g. "deck2 contents: 1 3 3 7"
    @Test
    public void testContentsLineFormat() {
        // TODO: deck 2 with 1, 3, 3, 7 added, check contentsLine() exactly
        fail("not written yet");
    }

    //an empty deck still writes its label
    @Test
    public void testContentsLineOfEmptyDeck() {
        // TODO: assertEquals("deck1 contents:", deck.contentsLine())
        fail("not written yet");
    }

    //changing the list from getContents must not change the real deck (no escape)
    @Test
    public void testGetContentsReturnsCopy() {
        // TODO: add a card, clear() the list returned by getContents(), check deck.size() is still 1
        fail("not written yet");
    }

    //writeOutput creates the file with the contents line in it
    @Test
    public void testWriteOutputCreatesFile() throws Exception {
        // TODO: add cards, writeOutput(), readLines("deck1_output.txt") has exactly one line equal to contentsLine()
        fail("not written yet");
    }

    //many threads adding and drawing at the same time lose no cards and draw no card twice
    @Test(timeout = 10000)
    public void testConcurrentAddsAndDrawsLoseNoCards() throws Exception {
        // TODO:
        //  1. start a few threads that each add lots of cards (each card a different value)
        //  2. start a few threads that each draw lots of times, keeping every non-null card
        //  3. join every thread
        //  4. drawn cards + cards left in the deck == cards added, and no value appears twice
        fail("not written yet");
    }
}
