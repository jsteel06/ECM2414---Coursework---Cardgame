import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

// CardGame should set up the ring correctly and play a whole game to the spec
public class CardGameTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private int winner;          // what play() returned in the last game
    private String printed;      // everything the last game printed to the terminal

    // full games write player and deck files into the current folder
    @After
    public void tearDown() {
        TestHelper.deleteOutputFiles(8);
    }

    // helpers methods

    // a random pack for n players that is garanteed to be won
    // it has at least four 1s, player 1 never discards a 1 and every other player always does
    // so player 1 must collect four 1s eventually if nobody else wins first
    private List<Card> winnablePack(int n, long seed) {
        List<Integer> values = PackGenerator.generate(n, n, new Random(seed));
        for (int i = 0; i < 4; i++) {
            values.set(4 * n + i, 1);    // four 1s go into the decks
        }
        List<Card> pack = new ArrayList<>();
        for (int value : values) {
            pack.add(new Card(value));
        }
        return pack;
    }

    // plays a game, remembering the winner and everything printed to the terminal
    private void playAndCapture(int n, List<Card> pack) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            winner = CardGame.play(n, pack);
        } finally {
            System.setOut(original);
        }
        printed = captured.toString();
    }

    // the lines the game printed to the terminal that announce a win
    private List<String> winLines() {
        List<String> lines = new ArrayList<>();
        for (String line : printed.lines().toList()) {
            if (line.contains("wins")) {
                lines.add(line);
            }
        }
        return lines;
    }

    // the hand line of a player's file, e.g. "player 2 hand: 1 3 3 4"
    private String handLine(int playerId) throws Exception {
        List<String> lines = TestHelper.readLines("player" + playerId + "_output.txt");
        return lines.get(lines.size() - 1);
    }



    @Test
    public void testCreateDecksNumberedOneToN() {
        List<CardDeck> decks = CardGame.createDecks(4);
        assertEquals(4, decks.size());
        for (int i = 0; i < 4; i++) {
            assertEquals(i + 1, decks.get(i).getId());
        }
    }

    //test for 5 decks
    @Test
    public void testCreateDecksNumberedOneToN_WithFive() {
        List<CardDeck> decks = CardGame.createDecks(5);
        assertEquals(5, decks.size());
    }

    // card i goes to player i % n, so player 1 gets card 1, 4, 7, 10 cards when n = 3
    @Test
    public void testDealHandsRoundRobin() {
        List<Card> pack = TestHelper.list(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24);
        List<Player> players = CardGame.createPlayers(3, CardGame.createDecks(3),
                new AtomicInteger(0), new CountDownLatch(3));

        CardGame.dealHands(pack, players, 3);

        assertEquals(List.of(1, 4, 7, 10), TestHelper.values(players.get(0).getHand()));
        assertEquals(List.of(2, 5, 8, 11), TestHelper.values(players.get(1).getHand()));
        assertEquals(List.of(3, 6, 9, 12), TestHelper.values(players.get(2).getHand()));
    }

    // remaining cards go round the decks in order, the first card each deck gets is on top
    @Test
    public void testFillDecksRoundRobin() {
        List<Card> pack = TestHelper.list(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24);
        List<CardDeck> decks = CardGame.createDecks(3);

        CardGame.fillDecks(pack, decks, 3);

        assertEquals(List.of(13, 16, 19, 22), TestHelper.values(decks.get(0).getContents()));
        assertEquals(List.of(14, 17, 20, 23), TestHelper.values(decks.get(1).getContents()));
        assertEquals(List.of(15, 18, 21, 24), TestHelper.values(decks.get(2).getContents()));
    }

    // player n draws from deck n and wraps round to discard to deck 1
    @Test
    public void testLastPlayerDiscardsToDeckOne() throws Exception {
        List<CardDeck> decks = CardGame.createDecks(3);
        List<Player> players = CardGame.createPlayers(3, decks, new AtomicInteger(0), new CountDownLatch(3));
        Player player3 = players.get(2);
        player3.setOutput(new BufferedWriter(Writer.nullWriter()));
        for (Card card : TestHelper.list(1, 3, 3, 3)) {
            player3.addToHand(card);
        }
        decks.get(2).addToBottom(new Card(5));

        player3.takeTurn();

        assertEquals(0, decks.get(2).size());                                        // drew the 5 from deck 3
        assertEquals(List.of(1), TestHelper.values(decks.get(0).getContents()));      // discarded the 1 to deck 1
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlayRejectsWrongPackSize() throws Exception {
        CardGame.play(2, winnablePack(2, 1).subList(0, 15));
    }



    // exactly 1 win line is printed and it matches the actual winner
    @Test(timeout = 10000)
    public void testGameHasExactlyOneWinner() throws Exception {
        playAndCapture(4, winnablePack(4, 1));

        assertTrue(winner >= 1 && winner <= 4);
        assertEquals(List.of("player " + winner + " wins"), winLines());
    }

    // winner's file ends with wins, exits, final hand, and the final hand is a winning hand
    @Test(timeout = 10000)
    public void testWinnerFileEndsWithWinningHand() throws Exception {
        playAndCapture(4, winnablePack(4, 2));

        List<String> lines = TestHelper.readLines("player" + winner + "_output.txt");
        int size = lines.size();
        assertEquals("player " + winner + " wins", lines.get(size - 3));
        assertEquals("player " + winner + " exits", lines.get(size - 2));
        assertTrue(lines.get(size - 1).startsWith("player " + winner + " final hand: "));

        List<Integer> finalHand = TestHelper.valuesFromString(lines.get(size - 1));
        assertEquals(4, finalHand.size());
        assertEquals(4, Collections.frequency(finalHand, finalHand.get(0)));
    }

    // every player finishes holding exactly 4 cards
    @Test(timeout = 10000)
    public void testEveryPlayerEndsWithFourCards() throws Exception {
        playAndCapture(4, winnablePack(4, 3));

        for (int i = 1; i <= 4; i++) {
            assertEquals("player " + i, 4, TestHelper.valuesFromString(handLine(i)).size());
        }
    }

    // the 8n cards are all still there at the end, in the final hands and the deck files
    @Test(timeout = 10000)
    public void testCardsAreConservedAcrossHandsAndDecks() throws Exception {
        List<Card> pack = winnablePack(4, 4);
        playAndCapture(4, pack);

        List<Integer> atTheEnd = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            atTheEnd.addAll(TestHelper.valuesFromString(handLine(i)));
            atTheEnd.addAll(TestHelper.valuesFromString(TestHelper.readLines("deck" + i + "_output.txt").get(0)));
        }
        List<Integer> atTheStart = TestHelper.values(pack);
        Collections.sort(atTheEnd);
        Collections.sort(atTheStart);
        assertEquals(atTheStart, atTheEnd);
    }

    // test the 2n output files, and each deck file is a single line
    @Test(timeout = 10000)
    public void testAllOutputFilesWritten() throws Exception {
        playAndCapture(4, winnablePack(4, 5));

        for (int i = 1; i <= 4; i++) {
            assertTrue("player" + i + "_output.txt", new File("player" + i + "_output.txt").exists());
            List<String> deckLines = TestHelper.readLines("deck" + i + "_output.txt");
            assertEquals(1, deckLines.size());
            assertTrue(deckLines.get(0).startsWith("deck" + i + " contents:"));
        }
    }

    // each file starts with the hand that was dealt and losers end with informed, exits then hand
    @Test(timeout = 10000)
    public void testPlayerFilesStartAndEndInSpecFormat() throws Exception {
        List<Card> pack = winnablePack(4, 6);
        playAndCapture(4, pack);

        for (int i = 1; i <= 4; i++) {
            List<String> lines = TestHelper.readLines("player" + i + "_output.txt");

            // player i was dealt cards i-1, i-1+n, i-1+2n and i-1+3n of the pack
            String dealt = pack.get(i - 1) + " " + pack.get(i + 3) + " " + pack.get(i + 7) + " " + pack.get(i + 11);
            assertEquals("player " + i + " initial hand " + dealt, lines.get(0));

            if (i != winner) {
                int size = lines.size();
                assertEquals("player " + winner + " has informed player " + i + " that player " + winner + " has won",
                        lines.get(size - 3));
                assertEquals("player " + i + " exits", lines.get(size - 2));
                assertTrue(lines.get(size - 1).startsWith("player " + i + " hand: "));
            }
        }
    }

    // a winning hand at the start means nobody plays, so every player file has exactly 4 lines
    // player 1 could win on its first turn
    // so this fails if anyone is allowed to play before every starting hand has been checked
    @Test(timeout = 20000)
    public void testImmediateWinGivesFourLineFiles() throws Exception {
        // dealt round-robin: player 1 gets 1 1 1 2, player 2 gets 7 7 7 7, player 3 gets 3 5 6 8, player 4 gets 4 5 6 8
        List<Card> pack = TestHelper.list(
                1, 7, 3, 4,   1, 7, 5, 5,   1, 7, 6, 6,   2, 7, 8, 8,
                1, 2, 3, 4,   2, 3, 4, 5,   2, 3, 4, 5,   2, 3, 4, 5);

        // threads are nondeterministic so try a few times
        for (int game = 0; game < 20; game++) {
            playAndCapture(4, pack);

            assertEquals(2, winner);
            assertEquals(List.of("player 2 wins"), winLines());
            for (int i = 1; i <= 4; i++) {
                assertEquals("player " + i + " file, game " + game,
                        4, TestHelper.readLines("player" + i + "_output.txt").size());
            }
            // nobody played, so the decks are exactly as they were filled
            assertEquals("deck1 contents: 1 2 2 2", TestHelper.readLines("deck1_output.txt").get(0));
            assertEquals("deck4 contents: 4 5 5 5", TestHelper.readLines("deck4_output.txt").get(0));
            TestHelper.deleteOutputFiles(4);
        }
    }

    // a single player game still runs and ends, drawing from and discarding to the same deck
    @Test(timeout = 10000)
    public void testSinglePlayerGame() throws Exception {
        playAndCapture(1, TestHelper.list(1, 2, 3, 4, 1, 1, 1, 5));

        assertEquals(1, winner);
        assertEquals("player 1 final hand: 1 1 1 1", handLine(1));
        assertTrue(new File("deck1_output.txt").exists());
    }

    // threads are nondeterministic so play multiple games, every one must finish with exactly one winner
    @Test(timeout = 60000)
    public void testManyGamesAllFinish() throws Exception {
        for (int n = 1; n <= 6; n++) {
            for (int seed = 0; seed < 5; seed++) {
                playAndCapture(n, winnablePack(n, seed));
                assertTrue(winner >= 1 && winner <= n);
                assertEquals(1, winLines().size());
                TestHelper.deleteOutputFiles(n);
            }
        }
    }



    // main keeps asking until both inputs are valid, then plays
    @Test(timeout = 10000)
    public void testMainRepromptsUntilInputsAreValid() throws Exception {
        File pack = TestHelper.writePackFile(folder.getRoot(), "pack.txt", "1", "1", "1", "1", "2", "3", "4", "5");
        String missing = new File(folder.getRoot(), "missing.txt").getPath();
        String typed = "abc\n0\n1\n" + missing + "\n" + pack.getPath() + "\n";

        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(typed.getBytes()));
            System.setOut(new PrintStream(captured));
            CardGame.main(new String[0]);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        String output = captured.toString();

        // first two should be rejected, so the question is asked three times
        assertEquals(3, output.split("Please enter the number of players", -1).length - 1);
        // the missing file is rejected, so the pack question is asked twice
        assertEquals(2, output.split("Please enter location of pack to load", -1).length - 1);
        assertTrue(output.contains("Invalid pack"));
        // then the game is played, player 1 starts with wining hand
        assertTrue(output.contains("player 1 wins"));
    }
}
