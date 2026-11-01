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




        //shared by every player: 0 means no winner yet, otherwise the winner's id
        AtomicInteger winner = new AtomicInteger(0);
        //starts at n, each player counts down once it has checked its starting hand
        CountDownLatch ready = new CountDownLatch(n);

        // TODO: 2. decks = createDecks(n)
        //    3.  List<Player> players = createPlayers(n, decks, winner, ready);
        //TODO
        //  4. dealHands(pack, players, n)
        //  5. fillDecks(pack, decks, n)
        //  6. runGame(players)start the player threads and wait for them to finish
        //  7. writeDeckOutputs(decks) only after every player thread has finished
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
            System.out.println("Invalid number of players: please enter a positive integer.");
        }
    }

    //keeps asking until the user gives a valid pack file then returns its cards
    //PackLoader does the checking
    private static List<Card> askForPackLocation(Scanner input, int n) {
        while (true) {
            System.out.println("Please enter location of pack to load:");
            String path = input.nextLine().trim();

            //removes "" around the path string
            if (path.length() >= 2 && path.startsWith("\"") && path.endsWith("\"")) {
                path = path.substring(1, path.length() - 1);
            }

            try {
                return PackLoader.load(path, n);
            } catch (PackLoader.InvalidPackException e) {
                //tell the user what is wrong then ask again
                System.out.println("Invalid pack: " + e.getMessage());
            }
        }
    }





    //decks numbered 1 to n, index: 1 to n-1
    static List<CardDeck> createDecks(int n) {
        List<CardDeck> decks = new ArrayList<>();
        // TODO: add a CardDeck 1 to n
        return decks;
    }

    //player i draws from deck i and discards to deck i+1 except player n discards to deck 1
    //every player shares the same winner and ready latch
    static List<Player> createPlayers(int n, List<CardDeck> decks, AtomicInteger winner, CountDownLatch ready) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            CardDeck drawDeck = decks.get(i - 1);   //index is id - 1
            CardDeck discardDeck = decks.get(i % n);    //usually at index i but wraps round to deck 1 for player n
            players.add(new Player(i, drawDeck, discardDeck, winner, ready)); //adds new player to list
        }
        return players;
    }

    //deals cards one at a time to each player starting at player 1
    static void dealHands(List<Card> pack, List<Player> players, int n) {
        //      TODO: starting at index 0, go to card 4n-1 and give card to each player's hand respectively
    }

    //remaining cards go one at a time to each deck starting at deck 1
    static void fillDecks(List<Card> pack, List<CardDeck> decks, int n) {
        // TODO: while there are still cards in the pack, give card to each deck respectively
    }



    //starts every player thread, then waits until they have all finished
    static void runGame(List<Player> players) {
        // TODO: create a Thread for each player
        // TODO: start ALL the threads first
        // TODO: then join() all the threads main
    }

    //writes deckX_output.txt for every deck
    static void writeDeckOutputs(List<CardDeck> decks) {
        // TODO: call writeOutput() on every deck
    }
}