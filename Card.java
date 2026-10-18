public final class Card {
    private final int value;

    //constructor
    public Card(int value) {
        // validation: no none neg
        if (value < 0) {
            throw new IllegalArgumentException("Card values must be non-negative.");
        }
        this.value = value;
    }

    //value getter
    public int getValue() { return value; }

    //lets us print a card vaule directly
    @Override
    public String toString() { return String.valueOf(value); }
}

