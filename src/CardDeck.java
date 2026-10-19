import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


public class CardDeck {
    //attributes
    private final Deque<Card> cards = new ArrayDeque<>();     //deck: queue, FIFO
    private final int id;     //deck id: int

    //constructor just needs id
    public CardDeck(int id) {
        this.id = id;
    }

    //id getter
    public int getId() {
        return id;
    }

    //puts a card at bottom of deck
    public synchronized void addToBottom(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("cannot add a null card");
        }
        cards.addLast(card);
    }

    //takes a card from top of deck
    public synchronized Card drawFromTop() {
        return cards.pollFirst();
    }

    //checks wether deck is empty
    public synchronized boolean isEmpty() {
        return cards.isEmpty();
    }

    //returns the size of the deck
    public synchronized int size() {
        return cards.size();
    }

    //returns a copy of the Array
    public synchronized List<Card> getContents() {
        return new ArrayList<>(cards);
    }

    //returns the string of content of deck as specified in spec
    public synchronized String contentsLine() {
        StringBuilder sb = new StringBuilder("deck" + id + " contents:");
        for (Card card : cards) {
            sb.append(' ').append(card);
        }
        return sb.toString();
    }

    //writes content of deck to file
    public void writeOutput() {
        try (FileWriter writer = new FileWriter("deck" + id + "_output.txt")) {
            writer.write(contentsLine() + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Could not write output for deck " + id + ": " + e.getMessage());
        }
    }
}