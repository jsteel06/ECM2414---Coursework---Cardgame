import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//a player in the ring, each one has its own thread
//player i draws from deck i and discards to deck i+1 (or 1)
public class Player implements Runnable {

    //attributes
    private final int id;                 // player id
    private final CardDeck drawDeck;      // deck to the left
    private final CardDeck discardDeck;   // deck to the right
    private final AtomicInteger winner;   // shared by all players, 0 is no winner yet, otherwise its the id of the winner

    //oldest card at index 0, newest at the end.
    //discarding the oldest non-preferred card means no card is held forever as stated in spec
    private final List<Card> hand = new ArrayList<>();

    private BufferedWriter out;           // outputs kept in buffer before writing to output for optimization

    //constructor
    public Player(int id, CardDeck drawDeck, CardDeck discardDeck, AtomicInteger winner) {
        this.id = id;
        this.drawDeck = drawDeck;
        this.discardDeck = discardDeck;
        this.winner = winner;
    }

    //before game starts, deals cards to hand
    public synchronized void addToHand(Card card) {
        //TODO:reject null card, reject a 5th card, otherwise add to the end of hand
    }

    //returns a copy of the hand
    public synchronized List<Card> getHand() {
        return new ArrayList<>(hand);
    }

    //checks whether it has wining hand
    public synchronized boolean hasWinningHand() {
        // TODO:true if 4 cards have the same value. ANY VALUE not just preferred one
        return false;
    }

    //the oldest card in the hand that is not this player's preferred value.
    private Card chooseDiscard() {
        // TODO: loop through hand from index 0 and return the first card where value != id
        return null;
    }


    //atomic draw & discard.
    //returns false if the draw deck was empty
    boolean takeTurn() throws IOException {

        //java prevents a self deadlock automatically when 1 player and 1 deck
        //declared outside the if so they still exist for the synchronized blocks
        CardDeck first;
        CardDeck second;

        //since both decks are locked, lower id first, to prevent deadlock
        if (drawDeck.getId() < discardDeck.getId()) {
            first = drawDeck;
            second = discardDeck;
        } else {
            first = discardDeck;
            second = drawDeck;
        }

        synchronized (first) {
            synchronized (second) {
                synchronized (this) {
                    // TODO:
                    //   - if drawDeck is empty return false
                    //   - draw from top of drawDeck add to end of hand
                    //   - pick a card with chooseDiscard remove it, add to bottom of discardDeck
                    //   - log the three lines: draws, discards, current hand
                    return true;
                }
            }
        }
    }


    //this is the method that has it's own thread.
    @Override
    public void run() {
        // TODO: open the output file, then:
        //   1. log initial hand
        //   2. if hasWinningHand() then declareWin()
        //   3. otherwise loop while winner == 0:
        //        if takeTurn() returned false then Thread.yield() and try again
        //        elif hasWinningHand() then declareWin()
        //   4. write the end-of-game lines (writeWinLines() or writeLoseLines())
    }


    //id getter
    public int getId() {
        return id;
    }

    //only the first player to call this wins
    private void declareWin() {
        //TODO: if winner is 0, set winner to id and print "player i wins" to the terminal
        // USE compareAndSet() when doing this to keep it atomic
    }

    //writes one line to the output file
    private void log(String line) throws IOException {
        //TODO: write the line then a \n to out
    }

    //turns the hand into a string e.g. "4 4 1 2"
    private String handString() {
        //TODO: loop through hand and add value of card to string
        return "";
    }

    //last 3 lines of the file if this player won
    private void writeWinLines() throws IOException {
        //TODO: "player i wins", "player i exits", "player i final hand: ..."
    }

    //last 3 lines of the file if another player won
    private void writeLoseLines() throws IOException {
        //TODO: "player w has informed player i that player w has won",
        //  "player i exits", "player i hand: ..."
    }
}
