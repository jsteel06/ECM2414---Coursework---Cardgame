# ECM2414---Coursework---Cardgame
Card game coursework

# JDK 16+ & JUnit4 is required.

Put the game files, test files and lib in one folder in cardsTest

# To compile the tests, write the following command in a fresh terminal:

Windows: javac -cp ".;lib/*" *.java

Mac/Linux: javac -cp ".:lib/*" *.java

# Test Suite

To run the tests, write the following command:

windows: java -cp ".;lib/*" org.junit.runner.JUnitCore AllTests

Mac/Linux: java -cp ".:lib/*" org.junit.runner.JUnitCore AllTests

The tests will write and delete player output files and card deck output files - keep this in mind. The Test suite may take up to minute depending on the speed of your computer. The tests also use actual threads

If you'd like to run a single test, just write the given test instead of AllTests e.g:

java -cp ".;lib/*" org.junit.runner.JUnitCore CardTest

# HOW TO RUN THE GAME!

Write the following commands in a new terminal within the correct source folder if you have a valid card pack:

javac *.java

java CardGame

Then answer the prompt and type your the name of your card pack, including the extension type (.txt).

If you do not have your own valid card pack, use the PackGenerator with the following command (after you've compiled):

java PackGenerator 

Then answer the questions to your liking. The pack generated can then be used in the CardGame. The game may also go on forever if the card pack does not contain a winnable set of cards, keep this in mind!