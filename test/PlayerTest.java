import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

// Player should keep 4 cards, follow the dicard strategy and take turns atomically
// takeTurn() is called directly here, so each player is given a StringWriter to log into with setOutput()
public class PlayerTest {

    private CardDeck leftDeck;        // player 1 draws from deck 1
    private CardDeck rightDeck;       // player 1 discards to deck 2
    private AtomicInteger winner;
    private Player player;
    private StringWriter log;         // collects what the player logs, instead of a file
    private BufferedWriter writer;

    private static final long PLAY_TIME = 3000;   // how long the two real player threads play for

    @Before
    public void setUp() {
        leftDeck = new CardDeck(1);
        rightDeck = new CardDeck(2);
        winner = new AtomicInteger(0);
        player = new Player(1, leftDeck, rightDeck, winner, new CountDownLatch(1));
        log = new StringWriter();
        writer = new BufferedWriter(log);
        player.setOutput(writer);
    }

    // the real player threads write player1_output.txt and player2_output.txt
    @After
    public void tearDown() {
        TestHelper.deleteOutputFiles(2);
    }

    // gives the player these cards, oldest first
    private void deal(Player p, int... values) {
        for (Card card : TestHelper.list(values)) {
            p.addToHand(card);
        }
    }

    // puts these cards in the deck, first value on top
    private void fill(CardDeck deck, int... values) {
        for (Card card : TestHelper.list(values)) {
            deck.addToBottom(card);
        }
    }

    // every card value in the player's hand and both decks, sorted, so before and after can be compared
    private List<Integer> allValues(Player p, CardDeck a, CardDeck b) {
        List<Integer> values = new ArrayList<>(TestHelper.values(p.getHand()));
        values.addAll(TestHelper.values(a.getContents()));
        values.addAll(TestHelper.values(b.getContents()));
        Collections.sort(values);
        return values;
    }

    @Test(expected = IllegalStateException.class)
    public void testFifthCardRejected() {
        deal(player, 1, 2, 3, 4);
        player.addToHand(new Card(5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCardRejected() {
        player.addToHand(null);
    }

    // changing the list from getHand must not change the real hand
    @Test
    public void testGetHandReturnsCopy() {
        deal(player, 1, 2, 3, 4);
        player.getHand().remove(0);
        assertEquals(4, player.getHand().size());
    }

    @Test
    public void testFourOfPreferredValueWins() {
        deal(player, 1, 1, 1, 1);
        assertTrue(player.hasWinningHand());
    }

    // redundent test to check if four 2s win as well
    @Test
    public void testFourTwosWins() {
        deal(player, 2, 2, 2, 2);
        assertTrue(player.hasWinningHand());
    }

    // test that four of any value wins, not just the prefered one
    @Test
    public void testFourOfNonPreferredValueStillWins() {
        deal(player, 5, 5, 5, 5);
        assertTrue(player.hasWinningHand());
    }

    // test that not all hands are wining hands
    @Test
    public void testMixedHandDoesNotWin() {
        deal(player, 1, 1, 1, 2);
        assertFalse(player.hasWinningHand());
    }

    // fewer than 4 cards in a hand cant be a wining hand
    @Test
    public void testFewerThanFourCardsDoesNotWin() {
        deal(player, 1, 1, 1);
        assertFalse(player.hasWinningHand());
    }

    // after a completed atomic turn the hand is back to exactly 4 cards
    @Test
    public void testHandStaysAtFourCardsAfterTurn() throws Exception {
        deal(player, 1, 2, 3, 4);
        fill(leftDeck, 5);
        assertTrue(player.takeTurn());
        assertEquals(4, player.getHand().size());
    }

    // draws from the top of the left deck and discards to the bottom of the right deck
    @Test
    public void testTurnDrawsFromTopAndDiscardsToBottom() throws Exception {
        deal(player, 1, 1, 1, 2);
        fill(leftDeck, 7, 8);
        fill(rightDeck, 9);

        player.takeTurn();

        assertEquals(List.of(8), TestHelper.values(leftDeck.getContents()));      // 7 was taken from the top
        assertEquals(List.of(9, 2), TestHelper.values(rightDeck.getContents()));  // 2 went to the bottom
        assertEquals(List.of(1, 1, 1, 7), TestHelper.values(player.getHand()));
    }

    // prefered cards are never discarded, even over many turns
    @Test
    public void testPreferredCardsAreNeverDiscarded() throws Exception {
        deal(player, 1, 1, 2, 3);
        for (int i = 0; i < 20; i++) {
            fill(leftDeck, 5);
        }

        for (int i = 0; i < 20; i++) {
            player.takeTurn();
        }

        assertFalse(TestHelper.values(rightDeck.getContents()).contains(1));
        assertEquals(2, Collections.frequency(TestHelper.values(player.getHand()), 1));
    }

    // the oldest non-prefered card goes first
    @Test
    public void testOldestNonPreferredCardDiscardedFirst() throws Exception {
        deal(player, 2, 1, 3, 4);
        fill(leftDeck, 5);

        player.takeTurn();

        assertEquals(List.of(2), TestHelper.values(rightDeck.getContents()));
        assertEquals(List.of(1, 3, 4, 5), TestHelper.values(player.getHand()));
    }

    // a non-prefered card can't be held forever
    // 9 is the newest of the 4 non prefered cards, so it should be gone after at most 4 turns
    @Test
    public void testNonPreferredCardIsNotHeldForever() throws Exception {
        deal(player, 2, 3, 4, 9);
        fill(leftDeck, 5, 5, 5, 5);

        // unrolled the loop because it's easier to read
        player.takeTurn();
        player.takeTurn();
        player.takeTurn();
        player.takeTurn();

        assertTrue(TestHelper.values(rightDeck.getContents()).contains(9));
        assertFalse(TestHelper.values(player.getHand()).contains(9));
    }

    // an empty draw deck means no turn, nothing changes and nothing is logged
    @Test
    public void testEmptyDrawDeckChangesNothing() throws Exception {
        deal(player, 1, 2, 3, 4);

        assertFalse(player.takeTurn());

        assertEquals(List.of(1, 2, 3, 4), TestHelper.values(player.getHand()));
        assertEquals(0, rightDeck.size());
        writer.flush();
        assertEquals("", log.toString());
    }

    // cards are only moved, never created or lost
    @Test
    public void testCardsAreConservedDuringTurns() throws Exception {
        deal(player, 1, 2, 3, 4);
        fill(leftDeck, 5, 6, 7, 8, 9, 10, 11, 12);
        List<Integer> before = allValues(player, leftDeck, rightDeck);

        // keep going until the left deck runs out
        while (player.takeTurn()) {
            assertEquals(4, player.getHand().size());
        }

        assertEquals(before, allValues(player, leftDeck, rightDeck));
    }

    // each turn writes the three lines in the spec's format
    @Test
    public void testTurnIsLoggedInSpecFormat() throws Exception {
        deal(player, 1, 1, 2, 4);
        fill(leftDeck, 4);

        player.takeTurn();
        writer.flush();

        List<String> lines = log.toString().lines().toList();
        assertEquals(List.of(
                "player 1 draws a 4 from deck 1",
                "player 1 discards a 2 to deck 2",
                "player 1 current hand is 1 1 4 4"), lines);
    }

    // with 1 player the same deck is drawn from and discarded to, and it must not deadlock
    @Test(timeout = 5000)
    public void testSinglePlayerUsesOneDeck() throws Exception {
        CardDeck onlyDeck = new CardDeck(1);
        Player solo = new Player(1, onlyDeck, onlyDeck, winner, new CountDownLatch(1));
        solo.setOutput(new BufferedWriter(Writer.nullWriter()));   // log lines not needed, so thrown away
        deal(solo, 1, 2, 3, 4);
        fill(onlyDeck, 5, 6, 7, 8);

        assertTrue(solo.takeTurn());

        assertEquals(List.of(1, 3, 4, 5), TestHelper.values(solo.getHand()));
        assertEquals(List.of(6, 7, 8, 2), TestHelper.values(onlyDeck.getContents()));
    }

    // concurrancy

    // two real player threads share both decks and play at the same time
    // while they play, the test keeps checking that both hands always have 4 cards and no cards are missing
    @Test(timeout = 10000)
    public void testNeighboursPlayingAtOnceKeepFourCardsEach() throws Exception {
        CardDeck deck1 = new CardDeck(1);
        CardDeck deck2 = new CardDeck(2);
        CountDownLatch ready = new CountDownLatch(2);
        Player player1 = new Player(1, deck1, deck2, winner, ready);
        Player player2 = new Player(2, deck2, deck1, winner, ready);

        // no value appears more than twice, so nobody can ever get four of a kind and the game won't end by itself
        deal(player1, 1, 3, 4, 5);
        deal(player2, 2, 3, 4, 5);
        fill(deck1, 6, 7, 8, 9);
        fill(deck2, 6, 7, 8, 9);

        Thread thread1 = new Thread(player1);
        Thread thread2 = new Thread(player2);
        thread1.start();
        thread2.start();

        try {
            // check while they play: lock in the same order as takeTurn
            // decks by id, then players
            // so nothing can move while counting, and this can't cause a deadlock
            long stopAt = System.currentTimeMillis() + PLAY_TIME;
            while (System.currentTimeMillis() < stopAt) {
                synchronized (deck1) {
                    synchronized (deck2) {
                        synchronized (player1) {
                            synchronized (player2) {
                                assertEquals(4, player1.getHand().size());
                                assertEquals(4, player2.getHand().size());
                                assertEquals(8, deck1.size() + deck2.size());
                            }
                        }
                    }
                }
            }
        } finally {
            // nobody can win, so the test plays the part of a player 3 who has won, which ends the game
            // in finally so the players always stop, even if a check above failed
            winner.set(3);
            thread1.join();
            thread2.join();
        }

        // after the game ends both players still hold exactly 4 cards
        assertEquals(4, player1.getHand().size());
        assertEquals(4, player2.getHand().size());
        assertEquals(8, deck1.size() + deck2.size());
    }
}
