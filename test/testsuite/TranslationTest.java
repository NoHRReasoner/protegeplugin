package testsuite;

import helpers.KB;
import helpers.Rule;
import helpers.Translation;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class TranslationTest {

	static Rule rule(String ruleFormat, Object... args) {
		return new Rule(ruleFormat, args);
	}

	private OWLDataFactory df;
	private KB kb;

	private OWLObjectIntersectionOf and(OWLClassExpression... concepts) {
		return df.getOWLObjectIntersectionOf(concepts);
	}

	@Test
	public void core() throws OWLOntologyCreationException,
			OWLOntologyStorageException, ParserException {
		kb = new KB();
		OWLDataFactory df = kb.getDataFactory();
		OWLClass[] A = kb.getConcepts(10);
		OWLObjectProperty[] P = kb.getRoles(13);
		OWLDataProperty[] D = kb.getDataRoles(5);
		OWLLiteral l = df.getOWLLiteral(1);
		OWLIndividual a = kb.getIndividual();
		OWLIndividual b = kb.getIndividual();
		@SuppressWarnings("unchecked")
		Translation[] translations = {

				tr(df.getOWLClassAssertionAxiom(A[0], a),
						rule("a%s(c%s).", A[0], a),
						rule("d%s(c%s):-tnot(n%s(c%s)).", A[0], a, A[0], a)),

				tr(df.getOWLObjectPropertyAssertionAxiom(P[0], a, b),
						rule("a%s(c%s,c%s).", P[0], a, b),
						rule("d%s(c%s,c%s):-tnot(n%s(c%s,c%s)).", P[0], a, b,
								P[0], a, b)),

				tr(df.getOWLDataPropertyAssertionAxiom(D[0], a, l),
						rule("a%s(c%s,c%s).", D[0], a, l),
						rule("d%s(c%s,c%s):-tnot(n%s(c%s,c%s)).", D[0], a, l,
								D[0], a, l)),

				tr(df.getOWLSubClassOfAxiom(A[1], A[2]),
						subsumptionRules(A[1], A[2])),

				tr(df.getOWLSubClassOfAxiom(A[3], some(P[1])),
						rule("dom(a%s)(X):-a%s(X).", P[1], A[3]),
						rule("dom(d%s)(X):-d%s(X),tnot(n%s(X,_)).", P[1], A[3],
								P[1]), rule("n%s(X):-n%s(X,_).", A[3], P[1])),

				tr(df.getOWLSubClassOfAxiom(A[4], some(inv(P[2]))),
						rule("ran(a%s)(X):-a%s(X).", P[2], A[4]),
						rule("ran(d%s)(X):-d%s(X),tnot(n%s(_,X)).", P[2], A[4],
								P[2]), rule("n%s(X):-n%s(_,X).", A[4], P[2])),

				tr(df.getOWLSubClassOfAxiom(some(P[3]), A[5]),
						rule("a%s(X):-dom(a%s)(X).", A[5], P[3]),
						rule("d%s(X):-dom(d%s)(X),tnot(n%s(X)).", A[5], P[3],
								A[5]), rule("n%s(X,_):-n%s(X).", P[3], A[5])),

				tr(df.getOWLSubClassOfAxiom(some(inv(P[4])), A[6]),
						rule("a%s(X):-ran(a%s)(X).", A[6], P[4]),
						rule("d%s(X):-ran(d%s)(X),tnot(n%s(X)).", A[6], P[4],
								A[6]), rule("n%s(_,X):-n%s(X).", P[4], A[6])),

				tr(df.getOWLSubObjectPropertyOfAxiom(P[5], P[6]),
						subsumptionRules(P[5], P[6])),

				tr(df.getOWLSubObjectPropertyOfAxiom(P[7], inv(P[8])),
						rule("a%s(Y,X):-a%s(X,Y).", P[8], P[7]),
						rule("d%s(Y,X):-d%s(X,Y),tnot(n%s(Y,X)).", P[8], P[7],
								P[8]), rule("n%s(X,Y):-n%s(Y,X).", P[7], P[8])),

				tr(df.getOWLSubObjectPropertyOfAxiom(inv(P[9]), P[10]),
						rule("a%s(X,Y):-a%s(Y,X).", P[10], P[9]),
						rule("d%s(X,Y):-d%s(Y,X),tnot(n%s(X,Y)).", P[10], P[9],
								P[10]),
						rule("n%s(Y,X):-n%s(X,Y).", P[9], P[10])),

				tr(df.getOWLSubClassOfAxiom(A[7],
						df.getOWLObjectComplementOf(A[8])),
						disjunctionRules(A[7], A[8])),

				tr(df.getOWLDisjointObjectPropertiesAxiom(P[11], P[12]),
						disjunctionRules(P[11], P[12])),

				tr(df.getOWLSubDataPropertyOfAxiom(D[1], D[2]),
						rule("a%s(X,Y):-a%s(X,Y).", D[2], D[1]),
						rule("d%s(X,Y):-d%s(X,Y),tnot(n%s(X,Y)).", D[2], D[1],
								D[2]), rule("n%s(X,Y):-n%s(X,Y).", D[1], D[2])),

				tr(df.getOWLDisjointDataPropertiesAxiom(D[3], D[4]),
						rule("n%s(X,Y):-a%s(X,Y).", D[4], D[3]),
						rule("n%s(X,Y):-a%s(X,Y).", D[3], D[4])) };
		for (Translation tr : translations)
			tr.test();
	}

	private HashSet<Rule> disjunctionRules(OWLClass A1, OWLClass A2) {
		HashSet<Rule> result = new HashSet<Rule>();
		result.add(rule("n%s(X):-a%s(X).", A2, A1));
		result.add(rule("n%s(X):-a%s(X).", A1, A2));
		return result;
	}

	private HashSet<Rule> disjunctionRules(OWLObjectProperty P1,
			OWLObjectProperty P2) {
		HashSet<Rule> result = new HashSet<Rule>();
		result.add(rule("n%s(X,Y):-a%s(X,Y).", P1, P2));
		result.add(rule("n%s(X,Y):-a%s(X,Y).", P2, P1));
		return result;
	}
	
	
	
	
	@Test
	public void extended() throws OWLOntologyCreationException,
			OWLOntologyStorageException, ParserException {
		kb = new KB();
		df = kb.getDataFactory();
		OWLClass[] A = kb.getConcepts(20);
		OWLObjectProperty[] P = kb.getRoles(20);
		OWLDataProperty[] D = kb.getDataRoles(10);
		OWLSubClassOfAxiom someSub = df.getOWLSubClassOfAxiom(A[4],
				df.getOWLObjectSomeValuesFrom(P[0], A[5]));
		OWLObjectProperty pSomeSub = getNewRole(someSub.hashCode());
		OWLObjectProperty pSomeSub2 = getNewRole(df.getOWLSubClassOfAxiom(A[4], df.getOWLObjectSomeValuesFrom(P[0], A[5])).hashCode());
		OWLObjectProperty pSomeSub3 = getNewRole(df.getOWLSubClassOfAxiom(A[13], df.getOWLObjectSomeValuesFrom(P[15], A[14])).hashCode());
		Assert.assertEquals(pSomeSub, pSomeSub2);
		Assert.assertNotEquals(pSomeSub, pSomeSub3);
		@SuppressWarnings("unchecked")
		Translation[] translations = {

				tr(df.getOWLSubClassOfAxiom(A[0], and(A[1], A[2], A[3])),
						subsumptionRules(A[0], A[1]),
						subsumptionRules(A[0], A[2]),
						subsumptionRules(A[0], A[3])),

				tr(someSub,
						rule("a%s(X,Y):-a%s(X,Y).", P[0], pSomeSub),
						rule("d%s(X,Y):-d%s(X,Y),tnot(n%s(X,Y)).", P[0],
								pSomeSub, P[0]),
						rule("n%s(X,Y):-n%s(X,Y).", pSomeSub, P[0]),
						rule("a%s(X):-ran(a%s)(X).", A[5], pSomeSub),
						rule("d%s(X):-ran(d%s)(X),tnot(n%s(X)).", A[5],
								pSomeSub, A[5]),
						rule("n%s(_,X):-n%s(X).", pSomeSub, A[5]),
						rule("dom(a%s)(X):-a%s(X).", pSomeSub, A[4]),
						rule("dom(d%s)(X):-d%s(X),tnot(n%s(X,_)).", pSomeSub,
								A[4], pSomeSub),
						rule("n%s(X):-n%s(X,_).", A[4], pSomeSub)),

				tr(df.getOWLEquivalentClassesAxiom(A[5], A[6], A[7]),
						subsumptionRules(A[5], A[6]),
						subsumptionRules(A[6], A[5]),
						subsumptionRules(A[5], A[7]),
						subsumptionRules(A[7], A[5]),
						subsumptionRules(A[6], A[7]),
						subsumptionRules(A[7], A[6])),

				tr(df.getOWLDisjointClassesAxiom(A[8], A[9], A[12]),
						disjunctionRules(A[8], A[9]),
						disjunctionRules(A[8], A[12]),
						disjunctionRules(A[9], A[12])),

				tr(df.getOWLInverseObjectPropertiesAxiom(P[1], P[2]),
						rule("a%s(Y,X):-a%s(X,Y).", P[2], P[1]),
						rule("d%s(Y,X):-d%s(X,Y),tnot(n%s(Y,X)).", P[2], P[1],
								P[2]),
						rule("n%s(X,Y):-n%s(Y,X).", P[1], P[2]),
						rule("a%s(Y,X):-a%s(X,Y).", P[1], P[2]),
						rule("d%s(Y,X):-d%s(X,Y),tnot(n%s(Y,X)).", P[1], P[2],
								P[1]), rule("n%s(X,Y):-n%s(Y,X).", P[2], P[1])),

				tr(df.getOWLEquivalentObjectPropertiesAxiom(P[3], P[4], P[5]),
						subsumptionRules(P[3], P[4]),
						subsumptionRules(P[4], P[3]),
						subsumptionRules(P[3], P[5]),
						subsumptionRules(P[5], P[3]),
						subsumptionRules(P[4], P[5]),
						subsumptionRules(P[5], P[4])),

				tr(df.getOWLObjectPropertyDomainAxiom(P[6], A[10]),
						rule("a%s(X):-dom(a%s)(X).", A[10], P[6]),
						rule("d%s(X):-dom(d%s)(X),tnot(n%s(X)).", A[10], P[6],
								A[10]), rule("n%s(X,_):-n%s(X).", P[6], A[10])),

				tr(df.getOWLObjectPropertyRangeAxiom(P[7], A[11]),
						rule("a%s(X):-ran(a%s)(X).", A[11], P[7]),
						rule("d%s(X):-ran(d%s)(X),tnot(n%s(X)).", A[11], P[7],
								A[11]), rule("n%s(_,X):-n%s(X).", P[7], A[11])),

				tr(df.getOWLDisjointObjectPropertiesAxiom(P[8], P[9], P[10]),
						disjunctionRules(P[8], P[9]),
						disjunctionRules(P[8], P[10]),
						disjunctionRules(P[9], P[10])),

				tr(df.getOWLSymmetricObjectPropertyAxiom(P[11]),
						rule("a%s(Y,X):-a%s(X,Y).", P[11], P[11]),
						rule("d%s(Y,X):-d%s(X,Y),tnot(n%s(Y,X)).", P[11],
								P[11], P[11]),
						rule("n%s(X,Y):-n%s(Y,X).", P[11], P[11]),
						rule("a%s(X,Y):-a%s(Y,X).", P[11], P[11]),
						rule("d%s(X,Y):-d%s(Y,X),tnot(n%s(X,Y)).", P[11],
								P[11], P[11]),
						rule("n%s(Y,X):-n%s(X,Y).", P[11], P[11])),

				tr(df.getOWLReflexiveObjectPropertyAxiom(P[12]),
						rule("ran(a%s)(X):-dom(a%s)(X).", P[12], P[12]),
						rule("ran(d%s)(X):-dom(d%s)(X),tnot(n%s(_,X)).", P[12],
								P[12], P[12]),
						rule("n%s(X,_):-n%s(_,X).", P[12], P[12]),
						rule("dom(a%s)(X):-ran(a%s)(X).", P[12], P[12]),
						rule("dom(d%s)(X):-ran(d%s)(X),tnot(n%s(X,_)).", P[12],
								P[12], P[12]),
						rule("n%s(_,X):-n%s(X,_).", P[12], P[12])),

				tr(df.getOWLAsymmetricObjectPropertyAxiom(P[14]),
						rule("n%s(X,Y):-a%s(Y,X).", P[14], P[14]),
						rule("n%s(Y,X):-a%s(X,Y).", P[14], P[14]),
						rule("n%s(X,X).", P[14])),

				tr(df.getOWLEquivalentDataPropertiesAxiom(D[0], D[1], D[2]),
						subsumptionRules(D[0], D[1]),
						subsumptionRules(D[1], D[0]),
						subsumptionRules(D[0], D[2]),
						subsumptionRules(D[2], D[0]),
						subsumptionRules(D[1], D[2]),
						subsumptionRules(D[2], D[1]))

		// TODO check profile violation
		// , tr(df.getOWLIrreflexiveObjectPropertyAxiom(P[13]),
		// rule("n%s(_,X):-a%s(X,_).", P[13], P[13]),
		// rule("n%s(X,_):-a%s(_,X).", P[13], P[13]))

		};

		for (Translation tr : translations)
			tr.test();
	}

	//copy of NormalizedOntology method
	private OWLObjectProperty getNewRole(int hashCode) {
		IRI ontologyIri = kb.getOntology().getOntologyID().getOntologyIRI();
		String fragment = String.valueOf(hashCode);
		IRI ruleIri = IRI.create(ontologyIri + "#" + fragment);
		return df.getOWLObjectProperty(ruleIri);
	}
	
	@Test
	public void ignored() throws OWLOntologyCreationException,
			OWLOntologyStorageException, ParserException {
		kb = new KB();
		df = kb.getDataFactory();
		OWLClass[] A = kb.getConcepts(15);
		OWLObjectProperty[] P = kb.getRoles(15);
		OWLDataProperty[] D = kb.getDataRoles(15);
		OWLIndividual a = kb.getIndividual();
		OWLIndividual b = kb.getIndividual();
		OWLLiteral l = df.getOWLLiteral(1);
		Translation[] translations = {

				tr(df.getOWLClassAssertionAxiom(df.getOWLThing(), a)),
				tr(df.getOWLClassAssertionAxiom(df.getOWLNothing(), a)),
				tr(df.getOWLObjectPropertyAssertionAxiom(
						df.getOWLTopObjectProperty(), a, b)),
				tr(df.getOWLObjectPropertyAssertionAxiom(
						df.getOWLBottomObjectProperty(), a, b)),
				tr(df.getOWLDataPropertyAssertionAxiom(
						df.getOWLTopDataProperty(), a, l)),
				tr(df.getOWLDataPropertyAssertionAxiom(
						df.getOWLBottomDataProperty(), a, l)),
				tr(df.getOWLSubClassOfAxiom(A[0], df.getOWLThing())),
				tr(df.getOWLSubClassOfAxiom(df.getOWLThing(), A[4])),
				tr(df.getOWLSubClassOfAxiom(df.getOWLNothing(), A[1])),
				tr(df.getOWLSubClassOfAxiom(A[2],
						df.getOWLObjectComplementOf(df.getOWLNothing()))),
				tr(df.getOWLSubClassOfAxiom(df.getOWLNothing(),
						df.getOWLObjectComplementOf(A[3]))),
				tr(df.getOWLSubObjectPropertyOfAxiom(P[0],
						df.getOWLTopObjectProperty())),
				tr(df.getOWLSubObjectPropertyOfAxiom(
						df.getOWLTopObjectProperty(), P[2])),
				tr(df.getOWLSubObjectPropertyOfAxiom(
						df.getOWLBottomObjectProperty(), P[1])),
				tr(df.getOWLSubDataPropertyOfAxiom(D[0],
						df.getOWLTopDataProperty())),
				tr(df.getOWLSubClassOfAxiom(
						df.getOWLDataSomeValuesFrom(D[1],
								df.getIntegerOWLDatatype()), A[4])),
				tr(df.getOWLSubClassOfAxiom(
						A[5],
						df.getOWLDataSomeValuesFrom(D[2],
								df.getIntegerOWLDatatype()))),
				tr(df.getOWLDataPropertyDomainAxiom(D[3], A[6])),
				tr(df.getOWLDataPropertyRangeAxiom(D[4],
						df.getIntegerOWLDatatype())),
				tr(df.getOWLDifferentIndividualsAxiom(a, b)),
				tr(df.getOWLSubDataPropertyOfAxiom(
						df.getOWLBottomDataProperty(), D[1]))
		// profile violation
		// ,tr(df.getOWLSubDataPropertyOfAxiom(df.getOWLTopDataProperty(),
		// D[2])),

		// profile violation
		// ,tr(df.getOWLDisjointObjectPropertiesAxiom(P[1],
		// df.getOWLBottomObjectProperty()))
		};

		for (Translation tr : translations)
			tr.test();
	}

	private OWLObjectPropertyExpression inv(OWLObjectProperty P) {
		return kb.getInverse(P);
	}

	private OWLObjectSomeValuesFrom some(OWLObjectPropertyExpression P) {
		return kb.getExistential(P);
	}

	//
	@Test
	public void special() throws OWLOntologyCreationException,
			OWLOntologyStorageException, ParserException {
		kb = new KB();
		df = kb.getDataFactory();
		OWLClass[] A = kb.getConcepts(15);
		OWLObjectProperty[] P = kb.getRoles(15);
		OWLDataProperty[] D = kb.getDataRoles(2);
		Translation[] translations = {

				tr(df.getOWLSubClassOfAxiom(A[0], df.getOWLNothing()),
						rule("n%s(X).", A[0])),

				tr(df.getOWLSubClassOfAxiom(df.getOWLThing(),
						df.getOWLObjectComplementOf(A[2])),
						rule("n%s(X).", A[2])),

				tr(df.getOWLSubObjectPropertyOfAxiom(P[0],
						df.getOWLBottomObjectProperty()),
						rule("n%s(X,Y).", P[0])),

				tr(df.getOWLSubDataPropertyOfAxiom(D[0],
						df.getOWLBottomDataProperty()), rule("n%s(X,Y).", D[0]))

		// profile violation
		// , tr(df.getOWLDisjointObjectPropertiesAxiom(P[1],
		// df.getOWLTopObjectProperty()),
		// disjunctionRules(P[1], P[1]))
		};

		for (Translation tr : translations)
			tr.test();

	}

	private HashSet<Rule> subsumptionRules(OWLClass A1, OWLClass A2) {
		HashSet<Rule> result = new HashSet<Rule>();
		result.add(rule("a%s(X):-a%s(X).", A2, A1));
		result.add(rule("d%s(X):-d%s(X),tnot(n%s(X)).", A2, A1, A2));
		result.add(rule("n%s(X):-n%s(X).", A1, A2));
		return result;
	}

	private HashSet<Rule> subsumptionRules(OWLProperty<?, ?> P1,
			OWLProperty<?, ?> P2) {
		HashSet<Rule> result = new HashSet<Rule>();
		result.add(rule("a%s(X,Y):-a%s(X,Y).", P2, P1));
		result.add(rule("d%s(X,Y):-d%s(X,Y),tnot(n%s(X,Y)).", P2, P1, P2));
		result.add(rule("n%s(X,Y):-n%s(X,Y).", P1, P2));
		return result;
	}

	private Translation tr(OWLAxiom axiom) throws OWLOntologyCreationException {
		return new Translation(kb, axiom, new HashSet<Rule>());
	}

	private Translation tr(OWLAxiom axiom, HashSet<Rule>... ruleSets)
			throws OWLOntologyCreationException {
		Set<Rule> ruleSet = new HashSet<Rule>();
		for (Set<Rule> s : ruleSets)
			ruleSet.addAll(s);
		return new Translation(kb, axiom, ruleSet);
	}

	private Translation tr(OWLAxiom axiom, Rule... rules)
			throws OWLOntologyCreationException {
		Set<Rule> ruleSet = new HashSet<Rule>();
		for (Rule rule : rules)
			ruleSet.add(rule);
		return new Translation(kb, axiom, ruleSet);
	}

	private Set<OWLAxiom> set(OWLAxiom... elems) {
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		for (OWLAxiom e : elems)
			result.add(e);
		return result;

	}

	private Translation tru(Set<OWLAxiom> axioms,
			OWLEntity... unsatisfaibleEntities)
			throws OWLOntologyCreationException {
		Set<Rule> result = new HashSet<Rule>();
		for (OWLEntity e : unsatisfaibleEntities) {
			String ruleFormat = e instanceof OWLClass ? "n%s(X)." : "n%s(X,Y).";
			result.add(new Rule(ruleFormat, e));
		}
		return new Translation(kb, axioms, false, result);
	}

	private Translation trir(Set<OWLAxiom> axioms,
			OWLProperty... irreflexiveRoles)
			throws OWLOntologyCreationException {
		Set<Rule> result = new HashSet<Rule>();
		for (OWLProperty e : irreflexiveRoles) {
			String ruleFormat = "n%s(X,X).";
			result.add(new Rule(ruleFormat, e));
		}
		return new Translation(kb, axioms, false, result);
	}

	@Test
	public void unsatisfaibleEntities() throws OWLOntologyCreationException,
			OWLOntologyStorageException, ParserException {
		kb = new KB();
		df = kb.getDataFactory();
		OWLClass[] A = kb.getConcepts(30);
		OWLObjectProperty[] P = kb.getRoles(30);
		OWLDataProperty[] D = kb.getDataRoles(20);
		Translation[] translations = {

				// level 0
				tru(set(df.getOWLDisjointClassesAxiom(A[5], df.getOWLThing())),
						A[5]),
				tru(set(df.getOWLDisjointClassesAxiom(df.getOWLThing(), A[6])),
						A[6]),
				tru(set(df.getOWLSubClassOfAxiom(A[7], df.getOWLNothing())),
						A[7]),

				// tru(set(df.getOWLDisjointObjectPropertiesAxiom(P[8],
				// df.getOWLTopObjectProperty())), P[8]),
				// tru(set(df.getOWLDisjointObjectPropertiesAxiom(df.getOWLTopObjectProperty(),
				// P[9])), P[9]),
				tru(set(df.getOWLSubObjectPropertyOfAxiom(P[10],
						df.getOWLBottomObjectProperty())), P[10]),

				// tru(set(df.getOWLDisjointDataPropertiesAxiom(D[3],
				// df.getOWLTopDataProperty())), D[3]),
				// tru(set(df.getOWLDisjointDataPropertiesAxiom(df.getOWLTopDataProperty(),
				// D[4])), D[4]),
				tru(set(df.getOWLSubDataPropertyOfAxiom(D[5],
						df.getOWLBottomDataProperty())), D[5]),

				// level 1
				tru(set(df.getOWLSubClassOfAxiom(A[0], A[1]),
						df.getOWLSubClassOfAxiom(A[0], A[2]),
						df.getOWLDisjointClassesAxiom(A[1], A[2])), A[0]),

				tru(set(df.getOWLSubClassOfAxiom(some(P[0]), A[3]),
						df.getOWLSubClassOfAxiom(some(P[0]), A[4]),
						df.getOWLDisjointClassesAxiom(A[3], A[4])), P[0]),

				tru(set(df.getOWLSubClassOfAxiom(some(inv(P[1])), A[5]),
						df.getOWLSubClassOfAxiom(some(inv(P[1])), A[6]),
						df.getOWLDisjointClassesAxiom(A[5], A[6])), P[1]),

				tru(set(df.getOWLSubObjectPropertyOfAxiom(P[2], P[3]),
						df.getOWLSubObjectPropertyOfAxiom(P[2], P[4]),
						df.getOWLDisjointObjectPropertiesAxiom(P[3], P[4])),
						P[2]),

				tru(set(df.getOWLSubObjectPropertyOfAxiom(inv(P[5]), P[6]),
						df.getOWLSubObjectPropertyOfAxiom(inv(P[5]), P[7]),
						df.getOWLDisjointObjectPropertiesAxiom(P[6], P[7])),
						P[5]),

				tru(set(df.getOWLSubDataPropertyOfAxiom(D[0], D[1]),
						df.getOWLSubDataPropertyOfAxiom(D[0], D[2]),
						df.getOWLDisjointDataPropertiesAxiom(D[1], D[2])), D[0]),

				// level 2
				tru(set(df.getOWLSubClassOfAxiom(A[8], A[9]),
						df.getOWLSubClassOfAxiom(A[9], A[10]),
						df.getOWLSubClassOfAxiom(A[8], A[11]),
						df.getOWLSubClassOfAxiom(A[11], A[12]),
						df.getOWLDisjointClassesAxiom(A[10], A[12])), A[8]),

				tru(set(df.getOWLSubObjectPropertyOfAxiom(P[11], P[12]),
						df.getOWLSubObjectPropertyOfAxiom(P[12], P[13]),
						df.getOWLSubObjectPropertyOfAxiom(P[11], P[14]),
						df.getOWLSubObjectPropertyOfAxiom(P[14], P[15]),
						df.getOWLDisjointObjectPropertiesAxiom(P[13], P[15])),
						P[11]),

				// not normalized
				tru(set(df.getOWLSubClassOfAxiom(A[13], A[14]),
						df.getOWLSubClassOfAxiom(A[13], A[15]),
						df.getOWLSubClassOfAxiom(A[14],
								df.getOWLObjectComplementOf(A[15]))), A[13]),

				tru(set(df.getOWLSubClassOfAxiom(A[16],
						df.getOWLObjectIntersectionOf(A[17], A[18], A[19])),
						df.getOWLSubClassOfAxiom(A[16],
								df.getOWLObjectIntersectionOf(A[21], A[22],
										A[23])), df.getOWLSubClassOfAxiom(
								A[17], df.getOWLObjectIntersectionOf(
										df.getOWLObjectComplementOf(A[21]),
										A[24], A[25]))), A[16]),

				tru(set(df.getOWLEquivalentClassesAxiom(A[26], A[27]),
						df.getOWLDisjointClassesAxiom(A[26], A[27])), A[26],
						A[27]),

				tru(set(df
						.getOWLInverseObjectPropertiesAxiom(P[16], inv(P[17])),
						df.getOWLInverseObjectPropertiesAxiom(P[16], inv(P[18])),
						df.getOWLDisjointObjectPropertiesAxiom(P[17], P[18])),
						P[16]),

				tru(set(df.getOWLEquivalentObjectPropertiesAxiom(P[19], P[20]),
						df.getOWLDisjointObjectPropertiesAxiom(P[19], P[20])),
						P[19], P[20]),

				tru(set(df.getOWLEquivalentDataPropertiesAxiom(D[4], D[5]),
						df.getOWLDisjointDataPropertiesAxiom(D[4], D[5])),
						D[4], D[5]),

				tru(set(df.getOWLObjectPropertyDomainAxiom(P[21], A[27]),
						df.getOWLObjectPropertyDomainAxiom(P[21], A[28]),
						df.getOWLDisjointClassesAxiom(A[27], A[28])), P[21]),

				tru(set(df.getOWLObjectPropertyRangeAxiom(P[22], A[27]),
						df.getOWLObjectPropertyRangeAxiom(P[22], A[28]),
						df.getOWLDisjointClassesAxiom(A[27], A[28])), P[22]),

				tru(set(df.getOWLSymmetricObjectPropertyAxiom(P[23]),
						df.getOWLAsymmetricObjectPropertyAxiom(P[23])), P[23]),

				tru(set(df.getOWLReflexiveObjectPropertyAxiom(P[24]),
						df.getOWLDisjointClassesAxiom(some(P[24]),
								some(inv(P[24])))), P[24]) };

		for (Translation tr : translations)
			tr.test();
	}

	@Test
	public void irreflexiveRoles() throws OWLOntologyCreationException,
			OWLOntologyStorageException, ParserException {
		kb = new KB();
		df = kb.getDataFactory();
		OWLClass[] A = kb.getConcepts(30);
		OWLObjectProperty[] P = kb.getRoles(40);
		OWLDataProperty[] D = kb.getDataRoles(20);
		Translation[] translations = {

				// level 1
				trir(set(df.getOWLSubClassOfAxiom(some(P[0]), A[3]),
						df.getOWLSubClassOfAxiom(some(inv(P[0])), A[4]),
						df.getOWLDisjointClassesAxiom(A[3], A[4])), P[0]),

				trir(set(df.getOWLSubClassOfAxiom(some(inv(P[1])), A[5]),
						df.getOWLSubClassOfAxiom(some(P[1]), A[6]),
						df.getOWLDisjointClassesAxiom(A[5], A[6])), P[1]),

				trir(set(df.getOWLSubObjectPropertyOfAxiom(P[5], P[6]),
						df.getOWLSubObjectPropertyOfAxiom(inv(P[5]), P[7]),
						df.getOWLDisjointObjectPropertiesAxiom(P[6], P[7])),
						P[5]),

				trir(set(df.getOWLSubObjectPropertyOfAxiom(inv(P[8]), P[9]),
						df.getOWLSubObjectPropertyOfAxiom(P[8], P[10]),
						df.getOWLDisjointObjectPropertiesAxiom(P[9], P[10])),
						P[8]),

				// level 2

				trir(set(df.getOWLSubObjectPropertyOfAxiom(P[11], P[12]),
						df.getOWLSubObjectPropertyOfAxiom(P[12], P[13]),
						df.getOWLSubObjectPropertyOfAxiom(inv(P[11]), P[14]),
						df.getOWLSubObjectPropertyOfAxiom(P[14], P[15]),
						df.getOWLDisjointObjectPropertiesAxiom(P[13], P[15])),
						P[11]),

				trir(set(df.getOWLSubObjectPropertyOfAxiom(inv(P[11]), P[12]),
						df.getOWLSubObjectPropertyOfAxiom(P[12], P[13]),
						df.getOWLSubObjectPropertyOfAxiom(P[11], P[14]),
						df.getOWLSubObjectPropertyOfAxiom(P[14], P[15]),
						df.getOWLDisjointObjectPropertiesAxiom(P[13], P[15])),
						P[11]),

				// other
				trir(set(df.getOWLSubClassOfAxiom(some(P[16]), A[14]),
						df.getOWLSubClassOfAxiom(some(inv(P[16])), A[15]),
						df.getOWLSubClassOfAxiom(A[14],
								df.getOWLObjectComplementOf(A[15]))), P[16]),

				trir(set(df.getOWLSubClassOfAxiom(some(P[17]),
						df.getOWLObjectIntersectionOf(A[17], A[18], A[19])),
						df.getOWLSubClassOfAxiom(some(inv(P[17])),
								df.getOWLObjectIntersectionOf(A[21], A[22],
										A[23])), df.getOWLSubClassOfAxiom(
								A[17], df.getOWLObjectIntersectionOf(
										df.getOWLObjectComplementOf(A[21]),
										A[24], A[25]))), P[17]),		

				trir(set(df.getOWLObjectPropertyDomainAxiom(P[21], A[27]),
						df.getOWLObjectPropertyRangeAxiom(P[21], A[28]),
						df.getOWLDisjointClassesAxiom(A[27], A[28])), P[21]),

				trir(set(df.getOWLSymmetricObjectPropertyAxiom(P[23]),
						df.getOWLAsymmetricObjectPropertyAxiom(P[23])), P[23])		
		};
		
		for(Translation tr : translations)
			tr.test();

	}

}