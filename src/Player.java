import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

//a player in the ring, each one has its own thread
//player i draws from deck i and discards to deck i+1 (or 1)
public class Player implements Runnable {

    //attributes
    private final int id;                 // player id
    private final CardDeck drawDeck;      // deck to the left
    private final CardDeck discardDeck;   // deck to the right
    private final AtomicInteger winner;   // shared by all players, 0 is no winner yet, otherwise its the id of the winner
    private final CountDownLatch ready;   //every player counts down once it has checked its starting hand

    //oldest card at index 0, newest at the end.
    //discarding the oldest non-preferred card means no card is held forever as stated in spec
    private final List<Card> hand = new ArrayList<>();

    private BufferedWriter out;  // buffered for speed, only used by this player's  thread

    //constructor
    public Player(int id, CardDeck drawDeck, CardDeck discardDeck, AtomicInteger winner, CountDownLatch ready) {
        this.id = id;
        this.drawDeck = drawDeck;
        this.discardDeck = discardDeck;
        this.winner = winner;
        this.ready = ready;
    }


    //before game starts, deals cards to hand
    public synchronized void addToHand(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("cannot add a null card"); //card cant be null
        }
        if (hand.size() >= 4) {
            throw new IllegalStateException("player " + id + " already has 4 cards"); //hand cant contain more than 4 cards
        }
        hand.add(card);
    }

    //returns a copy of the hand
    public synchronized List<Card> getHand() {
        return new ArrayList<>(hand);
    }

    //checks whether it has wining hand
    public synchronized boolean hasWinningHand() {
        if (hand.size() != 4) {
            return false;
        } //winning hand has to be 4 cards

        int first = hand.get(0).getValue();
        for (Card card : hand) {
            if (card.getValue() != first) {
                return false;
            }
        } //compares card values to value of first card, if any are different then false
        return true; //only true after checks have been done
    }

    //returns the oldest card in the hand that is not this player's preferred value
    private Card chooseDiscard() {
        for (Card card : hand) {
            if (card.getValue() != id) {
                return card;
            }
        }
        return null;
    }


    //atomic draw & discard.
    //returns false if the draw deck was empty
    boolean takeTurn() throws IOException {

        //java prevents a self deadlock automatically when 1 player and 1 deck
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

        Card drawn;
        Card discarded;
        String currentHand;

        // three nested synchronized blocks hold both decks and this player at the same time to keep as one atomic action
        synchronized (first) {
            synchronized (second) {
                synchronized (this) {

                    drawn = drawDeck.drawFromTop(); //gets top card from deck, null if the deck is empty
                    if (drawn == null) {
                        return false;
                    }

                    hand.add(drawn); //add card to hand
                    //this is the only time hand will have 5 cards which is why we lock player aswell as the two decks

                    discarded = chooseDiscard(); //uses chooseDiscard method to choose a card to discard
                    if (discarded == null) {
                        throw new IllegalStateException("player " + id + " has no card it can discard");
                    }

                    hand.remove(discarded); //remove discarded card from hand
                    discardDeck.addToBottom(discarded); //add discarded card to bottom of discard deck

                    currentHand = handString(); //stores hand as string while still locked
                }
            }
        }

        //uses log method to log as to specification requirements
        log("player " + id + " draws a " + drawn + " from deck " + drawDeck.getId());
        log("player " + id + " discards a " + discarded + " to deck " + discardDeck.getId());
        log("player " + id + " current hand is " + currentHand);
        return true;
    }


    //this is the method that has it's own thread.
    @Override
    public void run() {
        if (hasWinningHand()) {
            declareWin();
        } //checks for winning hand as game starts

        ready.countDown(); //this player has checked its starting hand, done before opening the file so a file error won't cause deadlock

        //try with resources so the writer always closes and flushes, even when an exception
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("player" + id + "_output.txt"))) {
            out = writer; //stores the writer in the field so log() can use it

            log("player " + id + " initial hand " + handString()); //log method logs hand string

            ready.await(); //waits for all players to be ready

            while (winner.get() == 0) { //while there still isnt a winner
                if (!takeTurn()) { //takes a turn
                    Thread.yield();
                } // yields if deck is empty

                else if (hasWinningHand()) { //checks for winning hand
                    declareWin(); //if winningHand, then run declareWin() method
                }
            }

            //writes winning lines if this player is winner
            if (winner.get() == id) {
                writeWinLines();
            }
            //else writes loseing lines
            else {
                writeLoseLines();
            }
        } catch (IOException e) {
            System.err.println("player " + id + " could not write its output file: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    //id getter
    public int getId() {
        return id;
    }

    //only the first player to call this wins
    private void declareWin() {
        if (winner.compareAndSet(0, id)) { //use compare and set as to keep atomic
            System.out.println("player " + id + " wins"); //outputs win line if compare and set
        }
    }

    //lets tests give the player somewhere to log without running its thread
    void setOutput(BufferedWriter writer) {
        out = writer;
    }

    //writes one line to the output file
    private void log(String line) throws IOException {
        out.write(line); //writes line
        out.newLine(); //writes new line
    }

    //turns the hand into a string e.g. "4 4 1 2"
    private synchronized String handString() {
        StringBuilder sb = new StringBuilder(); //uses string builder to build string
        for (Card card : hand) {
            if (!sb.isEmpty()) { //adds space between cards, but no space at beginning
                sb.append(' ');
            }
            sb.append(card); //adds a card
        }
        return sb.toString(); //returns the string
    }

    //last 3 lines of the file if this player won
    private void writeWinLines() throws IOException {
        log("player " + id + " wins");
        log("player " + id + " exits");
        log("player " + id + " final hand: " + handString());
    }

    //last 3 lines of the file if another player won
    private void writeLoseLines() throws IOException {
        int w = winner.get();
        log("player " + w + " has informed player " + id + " that player " + w + " has won");
        log("player " + id + " exits");
        log("player " + id + " hand: " + handString());
    }
}
