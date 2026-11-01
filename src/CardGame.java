import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;


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

        // TODO:
        //  1. create the shared winner variable
        //  2. create a list of card decks: createDecks()
        //  3. create a list of players: createPlayers()
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

    //player i draws from deck i and discards to deck i+1 excpet player n discards to deck 1
    static List<Player> createPlayers(int n, List<CardDeck> decks, AtomicInteger winner) {
        List<Player> players = new ArrayList<>();
        // TODO: for i: 1 to n, (the indexs are ids-1)
        //         drawDeck    = decks.get(i - 1)
        //         discardDeck = decks.get(i % n)  //this wraps around to discard to 1 for player n
        //         add new Player(i, drawDeck, discardDeck, winner)
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