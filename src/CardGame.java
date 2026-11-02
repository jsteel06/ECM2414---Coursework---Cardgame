import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;


//asks for the number of players and the pack file, re-prompting until valid
//then sets up and starts the game
public class CardGame {

    public static void main(String[] args) {
        //create a tool to read what the user types in the console
        Scanner input = new Scanner(System.in);
        //ask user how many players
        int n = askForNumberOfPlayers(input);
        //ask the user where the card pack file is stored, keeps asking until it is valid
        List<Card> pack = askForPackLocation(input, n);


        // since both inputs are valid, uses play method to run game
        try {
            play(n, pack);
        } catch (InterruptedException e) {
            System.out.println("The game was interrupted before it finished.");
        }
    }

    //sets up and plays one game with a validated pack, returns the winner's id
    //separate from main so tests can run a whole game without typing into the console
    static int play(int n, List<Card> pack) throws InterruptedException {
        if (pack.size() != 8 * n) {
            throw new IllegalArgumentException("pack must have exactly " + (8 * n) + " cards");
        }

        //shared by every player: 0 means no winner, else its the winner's id
        AtomicInteger winner = new AtomicInteger(0);

        //starts at n, each player counts down once it has checked its starting hand
        //so each player can check if they start out with a winning hand before play starts
        CountDownLatch ready = new CountDownLatch(n);

        //creates a list of decks with create decks
        List<CardDeck> decks = createDecks(n);

        //creates list of players with create players
        List<Player> players = createPlayers(n, decks, winner, ready);

        //deals hand to each player using given pack
        dealHands(pack, players, n);

        //deals decks with remaing cards
        fillDecks(pack, decks, n);

        //method starts each players thread and only continues when all threads are finished
        runThreads(players);

        //every player thread has finished, so the decks can't change anymore
        writeDeckOutputs(decks);

        return winner.get();
    }


    //keeps asking until the user types a positive whole number
    private static int askForNumberOfPlayers(Scanner input) {
        while (true) {
            System.out.println("Please enter the number of players: ");
            String line = input.nextLine();
            try {
                int n = Integer.parseInt(line);
                if (n > 0) {
                    return n;
                }
            } catch (NumberFormatException e) {
                // not a whole number fall through to the error message below
            }
            System.out.println("Invalid number of players, please enter a positive integer");
        }
    }

    //keeps asking until the user gives a valid pack file then returns its cards
    //PackLoader does the checking
    private static List<Card> askForPackLocation(Scanner input, int n) {
        while (true) {
            System.out.println("Please enter location of pack to load:");
            String path = input.nextLine().trim();


            try {
                return PackLoader.load(path, n);
            } catch (PackLoader.InvalidPackException e) {
                //tell the user what is wrong then ask again
                System.out.println("Invalid pack: " + e.getMessage());
            }
        }
    }


    //decks numbered 1 to n, index: 0 to n-1
    static List<CardDeck> createDecks(int n) {
        List<CardDeck> decks = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            decks.add(new CardDeck(i));
        }
        return decks;
    }

    //player i draws from deck i and discards to deck i+1 except player n discards to deck 1
    //every player shares the same winner and ready latch
    static List<Player> createPlayers(int n, List<CardDeck> decks, AtomicInteger winner, CountDownLatch ready) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            CardDeck drawDeck = decks.get(i - 1);   //index is id-1
            CardDeck discardDeck = decks.get(i % n);    //usually at index i but wraps round to deck 1 for player n
            players.add(new Player(i, drawDeck, discardDeck, winner, ready)); //adds new player to list
        }
        return players;
    }

    //deals cards one at a time to each player starting at player 1
    static void dealHands(List<Card> pack, List<Player> players, int n) {
        //4n cards are dealt in the loop
        for (int i = 0; i < 4 * n; i++) {
            players.get(i % n).addToHand(pack.get(i)); //using modulo, allows for simple wrap around when dealing cards
        }
    }

    //remaining cards go one at a time to each deck starting at deck 1
    static void fillDecks(List<Card> pack, List<CardDeck> decks, int n) {
        //cards 4n to 8n-1, 4n is a multiple of n so i % n starts again at deck 1 at index 0
        for (int i = 4 * n; i < pack.size(); i++) {
            decks.get(i % n).addToBottom(pack.get(i));
        }
    }


    //starts every player thread, then waits until they have all finished
    static void runThreads(List<Player> players) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (Player player : players) {
            threads.add(new Thread(player, "player" + player.getId()));
        }

        //start every thread before waiting on any, so they all play at the same time
        for (Thread thread : threads) {
            thread.start();
        }

        //main waits here until every player thread has finished
        for (Thread thread : threads) {
            thread.join();
        }
    }

    //writes deckX_output.txt for every deck
    static void writeDeckOutputs(List<CardDeck> decks) {
        for (CardDeck deck : decks) {
            deck.writeOutput();
        }
    }
}