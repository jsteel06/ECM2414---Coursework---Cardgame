import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//runs every test class in one go
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CardTest.class,
        CardDeckTest.class,
        PackLoaderTest.class,
        PlayerTest.class,
        CardGameTest.class
})
public class AllTests {
}
