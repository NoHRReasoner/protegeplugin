/**
 * 
 */
package test;

import hybrid.query.model.Query;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * @author nunocosta
 *
 */
public class QueryTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private KB kb;

	private void assertAnswer(String query, String[] expectedAns) {
		ArrayList<ArrayList<String>> result = (new Query(kb.getOntology()))
				.query(query);
		Assert.assertNotEquals("Should have anwswers", "no answers found",
				result.get(1).get(0));
		for (int i = 0; i < expectedAns.length; i++)
			Assert.assertEquals("Should have the", expectedAns[i], result
					.get(1).get(i + 1));
	}

	private void assertConsistent(String query) {
		ArrayList<ArrayList<String>> result = (new Query(kb.getOntology()))
				.query(query);
		Assert.assertNotEquals("Should be consistent", "inconsistent", result
				.get(1).get(0));
	}

	private void assertInconsistent(String query) {
		ArrayList<ArrayList<String>> result = (new Query(kb.getOntology()))
				.query(query);
		Assert.assertEquals("Should be inconsistent", "inconsistent", result
				.get(1).get(0));
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void cln2() throws OWLOntologyCreationException {
		kb.clear();
		OWLClass A1 = kb.getConcept("A1");
		OWLClass A2 = kb.getConcept("A2");
		OWLClass A3 = kb.getConcept("A3");
		OWLIndividual a = kb.getIndividual("a");
		kb.addAssertion(A1, a);
		kb.addRule("A3(X):-A1(X)");
		assertConsistent("A3(X)");
		kb.addSubsumption(A1, A2);
		assertConsistent("A3(X)");
		kb.addDisjunction(A2, A3);
		assertInconsistent("A3(X)");
	}
	
	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void unsatisfiableConcepts() throws OWLOntologyCreationException {
		kb.clear();
		OWLClass A1 = kb.getConcept("A1");
		OWLClass A2 = kb.getConcept("A2");
		OWLClass A3 = kb.getConcept("A3");
		OWLIndividual a = kb.getIndividual("a");
		kb.addAssertion(A1, a);
		kb.addSubsumption(A1, A2);
		kb.addSubsumption(A2, A1);
		kb.addSubsumption(A3, A2);
		kb.addDisjunction(A1, A2);
		kb.addRule("A3(X):-A1(X).");
		assertInconsistent("A3(X)");
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void cln3() throws OWLOntologyCreationException {
		kb.clear();
		OWLObjectProperty P1 = kb.getRole("P1");
		OWLObjectProperty P2 = kb.getRole("P2");
		OWLClass A = kb.getConcept("A");
		OWLIndividual a = kb.getIndividual("a");
		OWLIndividual b = kb.getIndividual("b");
		kb.addAssertion(A, a);
		kb.addAssertion(P1, a, b);
		kb.addRule("P2(X,Y):-P1(X,Y)");
		assertConsistent("P2(X,Y)");
		kb.addSubsumption(P1, P2);
		assertConsistent("P2(X,Y)");
		kb.addDisjunction(A, kb.getExistential(P2));
		assertInconsistent("P2(X,Y)");
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void cln4() throws OWLOntologyCreationException {
		kb.clear();
		OWLObjectProperty P1 = kb.getRole("P1");
		OWLObjectProperty P2 = kb.getRole("P2");
		OWLClass A = kb.getConcept("A");
		OWLIndividual a = kb.getIndividual("a");
		OWLIndividual b = kb.getIndividual("b");
		kb.addAssertion(A, b);
		kb.addAssertion(P1, a, b);
		kb.addRule("P2(X,Y):-P1(X,Y)");
		assertConsistent("P2(X,Y)");
		kb.addSubsumption(P1, P2);
		assertConsistent("P2(X,Y)");
		kb.addDisjunction(A, kb.getExistential(kb.getInverse(P2)));
		assertInconsistent("P2(X,Y)");
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void cln5() throws OWLOntologyCreationException {
		kb.clear();
		OWLObjectProperty P1 = kb.getRole("P1");
		OWLObjectProperty P2 = kb.getRole("P2");
		OWLObjectProperty P3 = kb.getRole("P3");
		OWLIndividual a = kb.getIndividual("a");
		OWLIndividual b = kb.getIndividual("b");
		kb.addAssertion(P1, a, b);
		kb.addRule("P3(X,Y):-P1(X,Y)");
		assertConsistent("P3(X,Y)");
		kb.addSubsumption(P1, P2);
		assertConsistent("P3(X,Y)");
		kb.addDisjunction(P2, P3);
		assertInconsistent("P3(X,Y)");
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void disjointWithAtomicConcepts()
			throws OWLOntologyCreationException {
		kb.clear();
		OWLClass A1 = kb.getConcept("A1");
		OWLClass A2 = kb.getConcept("A2");
		OWLIndividual a = kb.getIndividual("a");
		kb.addAssertion(A1, a);
		kb.addRule("A2(X):-A1(X)");
		assertConsistent("A2(X)");
		kb.addDisjunction(A1, A2);
		assertInconsistent("A2(X)");
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void disjointWithAtomicRoles()
			throws OWLOntologyCreationException {
		kb.clear();
		OWLObjectProperty P1 = kb.getRole("P1");
		OWLObjectProperty P2 = kb.getRole("P2");
		OWLIndividual a = kb.getIndividual("a");
		OWLIndividual b = kb.getIndividual("b");
		kb.addAssertion(P1, a, b);
		assertConsistent("P2(X,Y)");
		kb.addRule("P2(X,Y):-P1(X,Y).");
		assertConsistent("P2(X,Y)");
		kb.addDisjunction(P1, P2);
		assertInconsistent("P2(X,Y)");
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void disjointWithExistentialsOnTheLeft()
			throws OWLOntologyCreationException {
		kb.clear();
		OWLObjectProperty P1 = kb.getRole("P1");
		OWLClass A2 = kb.getConcept("A2");
		OWLIndividual a = kb.getIndividual("a");
		OWLIndividual b = kb.getIndividual("b");
		kb.addAssertion(P1, a, b);
		kb.addRule("A2(X):-P1(X,_)");
		assertConsistent("A2(X)");
		kb.addDisjunction(kb.getExistential(P1), A2);
		assertInconsistent("A2(X)");
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void disjointWithInverseRoles()
			throws OWLOntologyCreationException {
		kb.clear();
		OWLObjectProperty P1 = kb.getRole("P1");
		OWLObjectProperty P2 = kb.getRole("P2");
		OWLIndividual a = kb.getIndividual("a");
		OWLIndividual b = kb.getIndividual("b");
		kb.addAssertion(P1, a, b);
		kb.addRule("P2(Y,X):-P1(X,Y).");
		assertConsistent("P2(Y,X)");
		kb.addDisjunction(P1, kb.getInverse(P2));
		assertInconsistent("P2(Y,X)");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		kb = new KB();
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void subsumptionsWithAtomicAndInverseRoles()
			throws OWLOntologyCreationException {
		kb.clear();
		OWLObjectProperty P1 = kb.getRole("P1");
		OWLObjectProperty P2 = kb.getRole("P2");
		OWLObjectProperty P3 = kb.getRole("P3");
		OWLIndividual a = kb.getIndividual("a");
		OWLIndividual b = kb.getIndividual("b");
		kb.addAssertion(P1, a, b);
		kb.addSubsumption(P1, kb.getInverse(P2));
		kb.addSubsumption(P2, P3);
		assertAnswer("P2(X,Y)", new String[] { "b", "a" });
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void subsumptionWithAtomicConcepts()
			throws OWLOntologyCreationException {
		kb.clear();
		OWLClass A1 = kb.getConcept("A1");
		OWLClass A2 = kb.getConcept("A2");
		OWLIndividual a = kb.getIndividual("a");
		kb.addAssertion(A1, a);
		kb.addSubsumption(A1, A2);
		assertAnswer("A2(X)", new String[] { "a" });
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void subsumptionWithAtomicRoles()
			throws OWLOntologyCreationException {
		kb.clear();
		OWLObjectProperty P1 = kb.getRole("P1");
		OWLObjectProperty P2 = kb.getRole("P2");
		OWLIndividual a = kb.getIndividual("a");
		OWLIndividual b = kb.getIndividual("b");
		kb.addAssertion(P1, a, b);
		kb.addSubsumption(P1, P2);
		assertAnswer("P2(X,Y)", new String[] { "a", "b" });
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void subsumptionWithExistentialsOnTheLeft()
			throws OWLOntologyCreationException {
		kb.clear();
		OWLObjectProperty P1 = kb.getRole("P1");
		OWLClass A2 = kb.getConcept("A2");
		OWLIndividual a = kb.getIndividual("a");
		OWLIndividual b = kb.getIndividual("b");
		kb.addAssertion(P1, a, b);
		kb.addSubsumption(kb.getExistential(P1), A2);
		assertAnswer("A2(X)", new String[] { "a" });
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void subsumptionWithExistentialsOnTheRight()
			throws OWLOntologyCreationException {
		kb.clear();
		OWLClass A1 = kb.getConcept("A1");
		OWLObjectProperty P2 = kb.getRole("P2");
		OWLClass A3 = kb.getConcept("A3");
		OWLIndividual a = kb.getIndividual("a");
		kb.addAssertion(A1, a);
		kb.addSubsumption(A1, kb.getExistential(P2));
		kb.addSubsumption(kb.getExistential(P2), A3);
		assertAnswer("A3(X)", new String[] { "a" });
	}

	/**
	 * Test method for {@link hybrid.query.model.Query#query(java.lang.String)}.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public final void subsumptionWithInverseRoles()
			throws OWLOntologyCreationException {
		kb.clear();
		OWLObjectProperty P1 = kb.getRole("P1");
		OWLObjectProperty P2 = kb.getRole("P2");
		OWLIndividual a = kb.getIndividual("a");
		OWLIndividual b = kb.getIndividual("b");
		kb.addAssertion(P1, a, b);
		kb.addSubsumption(P1, kb.getInverse(P2));
		assertAnswer("P2(X,Y)", new String[] { "b", "a" });
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

}
