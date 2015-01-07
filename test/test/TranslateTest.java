package test;

import java.util.ArrayList;
import java.util.HashSet;

import local.translate.CollectionsManager;
import local.translate.Translate;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class TranslateTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private void assertHasRule(Translate tr, String ruleFormat,
			boolean fromOntology, Object... ruleArgs) {
		CollectionsManager cm = tr.getCollectionsManager();
		HashSet<String> rules = fromOntology ? cm.getTranslatedOntologies()
				: cm.getTranslatedRules();
		Assert.assertTrue("Should contain the rule",
				rules.contains(String.format(ruleFormat, ruleArgs)));
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testProceedWithDisjoint() throws Exception {
		KB kb = new KB();
		OWLIndividual c1 = kb.getIndividual("a");
		OWLIndividual c2 = kb.getIndividual("b");
		OWLClass a1 = kb.getConcept("A1");
		OWLClass a2 = kb.getConcept("A2");
		OWLClass a3 = kb.getConcept("A3");
		OWLClass a4 = kb.getConcept("A4");
		OWLObjectProperty p1 = kb.getRole("P1");
		OWLObjectProperty p2 = kb.getRole("P2");
		OWLObjectProperty p3 = kb.getRole("P3");
		OWLObjectProperty p4 = kb.getRole("P4");
		String a1Lbl = kb.getLabel(a1);
		String a2Lbl = kb.getLabel(a2);
		String a3Lbl = kb.getLabel(a3);
		String a4Lbl = kb.getLabel(a4);
		String p1Lbl = kb.getLabel(p1);
		String p2Lbl = kb.getLabel(p2);
		String p3Lbl = kb.getLabel(p3);
		String p4Lbl = kb.getLabel(p4);
		String c1Lbl = kb.getLabel(c1);
		String c2Lbl = kb.getLabel(c2);
		kb.addAssertion(a1, c1);
		kb.addAssertion(p1, c1, c2);
		kb.addSubsumption(a1, a2);
		kb.addSubsumption(p1, p2);
		kb.addDisjunction(a3, a4);
		kb.addDisjunction(p3, p4);

		ArrayList<String> rules = new ArrayList<String>();
		rules.add("q1(X):-q2(X).");
		rules.add("Q1(X):-Q2(X).");
		rules.add("A1(X):-Q2(X).");
		rules.add("A3(X):-Q2(X).");

		Translate tr = new Translate(kb.getOntology());
		tr.proceed();
		tr.appendRules(rules);
		assertHasRule(tr, "a%s(c%s).", true, a1Lbl, c1Lbl);
		assertHasRule(tr, "d%s(c%s):-tnot(n%s(c%s)).", true, a1Lbl, c1Lbl,
				a1Lbl, c1Lbl);
		assertHasRule(tr, "a%s(c%s,c%s).", true, p1Lbl, c1Lbl, c2Lbl);
		assertHasRule(tr, "d%s(c%s,c%s):-tnot(n%s(c%s,c%s)).", true, p1Lbl,
				c1Lbl, c2Lbl, p1Lbl, c1Lbl, c2Lbl);
		assertHasRule(tr, "a%s(X):-a%s(X).", true, a2Lbl, a1Lbl);
		assertHasRule(tr, "d%s(X):-d%s(X),tnot(n%s(X)).", true, a2Lbl, a1Lbl,
				a2Lbl);
		assertHasRule(tr, "n%s(X):-n%s(X).", true, a1Lbl, a2Lbl);
		assertHasRule(tr, "a%s(X,Y):-a%s(X,Y).", true, p2Lbl, p1Lbl);
		assertHasRule(tr, "d%s(X,Y):-d%s(X,Y),tnot(n%s(X,Y)).", true, p2Lbl,
				p1Lbl, p2Lbl);
		assertHasRule(tr, "n%s(X,Y):-n%s(X,Y).", true, p1Lbl, p2Lbl);

		assertHasRule(tr, "n%s(X):-a%s(X).", true, a4Lbl, a3Lbl);
		assertHasRule(tr, "n%s(X):-a%s(X).", true, a3Lbl, a4Lbl);
		assertHasRule(tr, "n%s(X,Y):-a%s(X,Y).", true, p4Lbl, p3Lbl);
		assertHasRule(tr, "n%s(X,Y):-a%s(X,Y).", true, p3Lbl, p4Lbl);

		CollectionsManager cm = tr.getCollectionsManager();

		assertHasRule(tr, "a%s(X) :- a%s(X).", false, cm.getHashedLabel("q1"),
				cm.getHashedLabel("q2"));
		assertHasRule(tr, "a%s(X) :- a%s(X).", false, cm.getHashedLabel("Q1"),
				cm.getHashedLabel("Q2"));
		assertHasRule(tr, "a%s(X) :- a%s(X).", false, cm.getHashedLabel("A1"),
				cm.getHashedLabel("Q2"));
		assertHasRule(tr, "a%s(X) :- a%s(X).", false, cm.getHashedLabel("A3"),
				cm.getHashedLabel("Q2"));

		assertHasRule(tr, "d%s(X) :- d%s(X).", false, cm.getHashedLabel("q1"),
				cm.getHashedLabel("q2"), cm.getHashedLabel("q1"));
		assertHasRule(tr, "d%s(X) :- d%s(X).", false, cm.getHashedLabel("Q1"),
				cm.getHashedLabel("Q2"), cm.getHashedLabel("Q1"));
		assertHasRule(tr, "d%s(X) :- d%s(X).", false, cm.getHashedLabel("A1"),
				cm.getHashedLabel("Q2"), cm.getHashedLabel("A1"));
		// assertHasRule(tr, "d%s(X) :- d%s(X).", false,
		// cm.getHashedLabel("A3"), cm.getHashedLabel("Q2"),
		// cm.getHashedLabel("A3"));

	}

	@Test
	public final void testProceedWithoutDisjoint() throws Exception {
		KB kb = new KB();
		OWLIndividual c1 = kb.getIndividual("a");
		OWLIndividual c2 = kb.getIndividual("b");
		OWLClass a1 = kb.getConcept("A1");
		OWLClass a2 = kb.getConcept("A2");
		OWLObjectProperty p1 = kb.getRole("P1");
		OWLObjectProperty p2 = kb.getRole("P2");
		String c1Lbl = kb.getLabel(c1);
		String c2Lbl = kb.getLabel(c2);
		String a1Lbl = kb.getLabel(a1);
		String a2Lbl = kb.getLabel(a2);
		String p1Lbl = kb.getLabel(p1);
		String p2Lbl = kb.getLabel(p2);

		kb.addAssertion(a1, c1);
		kb.addAssertion(p1, c1, c2);
		kb.addSubsumption(a1, a2);
		kb.addSubsumption(p1, p2);

		ArrayList<String> rules = new ArrayList<String>();
		rules.add("q1(X):-q2(X).");
		rules.add("Q1(X):-Q2(X).");
		rules.add("A1(X):-Q2(X).");

		Translate tr = new Translate(kb.getOntology());
		tr.proceed();
		tr.appendRules(rules);
		assertHasRule(tr, "a%s(c%s).", true, a1Lbl, c1Lbl);
		assertHasRule(tr, "a%s(c%s,c%s).", true, p1Lbl, c1Lbl, c2Lbl);
		assertHasRule(tr, "a%s(X):-a%s(X).", true, a2Lbl, a1Lbl);
		assertHasRule(tr, "n%s(X):-n%s(X).", true, a1Lbl, a2Lbl);
		assertHasRule(tr, "a%s(X,Y):-a%s(X,Y).", true, p2Lbl, p1Lbl);
		assertHasRule(tr, "n%s(X,Y):-n%s(X,Y).", true, p1Lbl, p2Lbl);

		CollectionsManager cm = tr.getCollectionsManager();

		assertHasRule(tr, "a%s(X) :- a%s(X).", false, cm.getHashedLabel("q1"),
				cm.getHashedLabel("q2"));
		assertHasRule(tr, "a%s(X) :- a%s(X).", false, cm.getHashedLabel("Q1"),
				cm.getHashedLabel("Q2"));
		assertHasRule(tr, "a%s(X) :- a%s(X).", false, cm.getHashedLabel("A1"),
				cm.getHashedLabel("Q2"));

	}
}
