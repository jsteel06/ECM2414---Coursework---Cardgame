import org.junit.Test;

import static org.junit.Assert.*;

//Card should only hold non-negative values and never change
public class CardTest {

    //a negative card must be rejected
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeValueRejected() {
        // TODO: new Card(-1)
        fail("not written yet");
    }

    //0 is non-negative so it is a valid card
    @Test
    public void testZeroIsAllowed() {
        // TODO: new Card(0), check getValue() is 0
        fail("not written yet");
    }

    //toString gives just the value, the output files depend on this
    @Test
    public void testToStringIsTheValue() {
        // TODO: assertEquals("7", new Card(7).toString())
        fail("not written yet");
    }
}
