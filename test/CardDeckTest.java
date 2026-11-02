import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

// CardDeck is a thread-safe first in first out queue
public class CardDeckTest {

    private CardDeck deck;

    @Before
    public void setUp() {
        deck = new CardDeck(1);
    }

    // delete created output files using helper method
    @After
    public void tearDown() {
        TestHelper.deleteOutputFiles(2);
    }

    // tests for checks if card order follows FIFO
    @Test
    public void testDrawOrderIsFirstInFirstOut() {
        deck.addToBottom(new Card(1));
        deck.addToBottom(new Card(2));
        assertEquals(1, deck.drawFromTop().getValue());

        deck.addToBottom(new Card(3));
        assertEquals(2, deck.drawFromTop().getValue());
        assertEquals(3, deck.drawFromTop().getValue());
        assertEquals(0, deck.size());
    }

    // check if draw from empty deck return nul
    @Test
    public void testDrawFromEmptyDeckReturnsNull() {
        assertNull(deck.drawFromTop());

        // still null after the last card has gone
        deck.addToBottom(new Card(5));
        deck.drawFromTop();
        assertNull(deck.drawFromTop());
    }



    // a null card can't be added
    @Test(expected = IllegalArgumentException.class)
    public void testAddingNullCardRejected() {
        deck.addToBottom(null);
    }

    // checks if output strings are valid
    @Test
    public void testContentsLineFormat() {
        CardDeck deck2 = new CardDeck(2);
        for (Card card : TestHelper.list(1, 3, 3, 7)) {
            deck2.addToBottom(card);
        }
        assertEquals("deck2 contents: 1 3 3 7", deck2.contentsLine());
    }

    // checks if output string still works with an empty deck
    @Test
    public void testContentsLineOfEmptyDeck() {
        assertEquals("deck1 contents:", deck.contentsLine());
    }

    // changing the list from getContents() deck method must not change the original deck
    @Test
    public void testGetContentsReturnsCopy() {
        deck.addToBottom(new Card(4));
        List<Card> copy = deck.getContents();
        copy.remove(0);
        assertEquals(1, deck.size());
    }

    // test getting the id works
    @Test
    public void testGetIdReturnsCorrectId() {
        assertEquals(1, deck.getId());
    }

    // writeOutput creates the file with the contents line
    @Test
    public void testWriteOutputCreatesFile() throws Exception {
        deck.addToBottom(new Card(4));
        deck.addToBottom(new Card(2));
        deck.writeOutput();

        List<String> lines = TestHelper.readLines("deck1_output.txt");
        assertEquals(List.of("deck1 contents: 4 2"), lines);
    }

    // adds cards with values first, first + 1 ... to the bottom of a deck, run on its own thread
    private static class Adder implements Runnable {
        private final CardDeck deck;
        private final int first;
        private final int count;

        Adder(CardDeck deck, int first, int count) {
            this.deck = deck;
            this.first = first;
            this.count = count;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                deck.addToBottom(new Card(first + i));
            }
        }
    }

    // tries to draw from a deck a number of times, keeping every card it gets, run on its own thread
    private static class Drawer implements Runnable {
        private final CardDeck deck;
        private final int attempts;
        private final List<Card> drawn = new ArrayList<>();   // only used by this drawer's thread until join

        Drawer(CardDeck deck, int attempts) {
            this.deck = deck;
            this.attempts = attempts;
        }

        @Override
        public void run() {
            for (int i = 0; i < attempts; i++) {
                Card card = deck.drawFromTop();
                if (card != null) {
                    drawn.add(card);
                }
            }
        }

        // only call after the thread has finished
        List<Card> getDrawn() {
            return drawn;
        }
    }


    // two threads add cards while two other threads draw cards, all at the same time
    // afterwards no card should be lost and no card should have been drawn twice
    @Test(timeout = 10000)
    public void testConcurrentAddsAndDrawsLoseNoCards() throws Exception {
        // values 0-49999 and 50000-99999 so every card is different
        Adder adder1 = new Adder(deck, 0, 50000);
        Adder adder2 = new Adder(deck, 50000, 50000);
        Drawer drawer1 = new Drawer(deck, 50000);
        Drawer drawer2 = new Drawer(deck, 50000);

        Thread thread1 = new Thread(adder1);
        Thread thread2 = new Thread(adder2);
        Thread thread3 = new Thread(drawer1);
        Thread thread4 = new Thread(drawer2);

        // start all four before waiting on any, so they run at the same time
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        // wait for all four to finish
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        // every card is either still in the deck or was drawn by one of the drawers
        List<Card> allCards = new ArrayList<>(deck.getContents());
        allCards.addAll(drawer1.getDrawn());
        allCards.addAll(drawer2.getDrawn());

        // a set doesnt include repeats, so if a card was drawn twice the set is smaller
        Set<Integer> uniqueValues = Set.copyOf(TestHelper.values(allCards));

        assertEquals("cards lost or duplicated", 100000, allCards.size());
        assertEquals("a card was drawn twice", 100000, uniqueValues.size());
    }
}
