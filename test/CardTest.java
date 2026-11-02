import org.junit.Test;

import static org.junit.Assert.*;

//Card should only hold non-negative values and never change
public class CardTest {

    //a negative card must be rejected
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeValueRejected() {
        new Card(-1);
    }

    //0 is non-negative so it is a valid card
    @Test
    public void testZeroIsAllowed() {
        Card card = new Card(0);
        assertEquals(0, card.getValue());
    }

    //toString has to give the value as a string
    @Test
    public void testToStringIsTheValue() {
        assertEquals("7", new Card(7).toString());
    }
}
