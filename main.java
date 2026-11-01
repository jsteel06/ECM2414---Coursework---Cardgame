import java.util.Arrays;
import java.io.FileWriter;

public final class Card {
    private final int value;

    public Card(int value) {

        //validation
        if (value < 0) {
            throw new IllegalArgumentException("Card values must be non-negative.")
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}

public class CardDeck {
    Card[] cards;
    private final int id;

    public CardDeck(int size) {
        cards = new Card[size];
        for (int i = 0; i < size; i++) {
            cards[i] = new Card(i);
        }
    }

    //id system for each deck will allow the decks to write
    //to their own respective output files
    public void writeOutput() {
        try {
            FileWriter writer = new FileWriter("deck" + id + "_output.txt");
            for (Card card : cards) {
                writer.write(card.getValue() + " ");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}