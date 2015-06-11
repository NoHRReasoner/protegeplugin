import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * 
 */

/**
 * @author nunocosta
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ BasicLazyGraphClosureTest.class, ParserTest.class,
	QueryProcessorTest.class, QueryTest.class, TBoxGraphTest.class,
	XSBDatabaseTest.class })
public class AllTests {

}
