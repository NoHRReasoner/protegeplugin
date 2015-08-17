/**
 *
 */
package pt.unl.fct.di.centria.nohr.reasoner.translation.ontology.el;

import static pt.unl.fct.di.centria.nohr.model.Model.atom;
import static pt.unl.fct.di.centria.nohr.model.Model.ruleSet;
import static pt.unl.fct.di.centria.nohr.model.Model.var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

import pt.unl.fct.di.centria.nohr.model.Atom;
import pt.unl.fct.di.centria.nohr.model.Literal;
import pt.unl.fct.di.centria.nohr.model.Rule;
import pt.unl.fct.di.centria.nohr.model.Variable;
import pt.unl.fct.di.centria.nohr.model.predicates.Predicate;
import pt.unl.fct.di.centria.nohr.model.predicates.Predicates;

/**
 * Provides EL<sub>&perp;</sub><sup>+</sup> axiom translation operations according to {@link <a>A Correct EL Oracle for NoHR (Technical Report)</a>}.
 *
 * @author Nuno Costa
 */
public abstract class ELAxiomsTranslator {

	/**
	 * An variable "X".
	 */
	protected static final Variable X = var("X");

	/**
	 * An variable "Y".
	 */
	protected static final Variable Y = var("Y");

	/**
	 * Returns a specified literal array as a literal list.
	 *
	 * @param literals
	 *            an array of literals.
	 * @return {@code literals} as a {@link List}.
	 */
	protected List<Literal> literalsList(Literal... literals) {
		return new ArrayList<>(Arrays.asList(literals));
	}

	/**
	 * Translate an role composition to a list of atoms according to <b> Definition 12.</b> of {@link <a>A Correct EL Oracle for NoHR (Technical
	 * Report)</a>}. Corresponds to <i>tr(R<sub>1k</sub>, x, x<sub>k</sub>)</i> and <i>tr(R<sub>1k</sub>, x, x<sub>k</sub>)<sup>d</sup></i>.
	 *
	 * @param chain
	 *            a role composition R<sub>1</sub>&#x26AA; ...&#x26AA;R<sub>k</sub>.
	 * @param x
	 *            a variable <i>x</i>.
	 * @param xk
	 *            a variable <i>x<sub>k</sub>.
	 * @param doub
	 *            specifies whether the returned axioms will be doubled (i.e. with double meta-predicates functors).
	 * @return <i>[R<sub>1</sub><sup>d</sup>(x, x<sub>1</sub>), ..., R<sub>k</sub><sup>d</sup>(x<sub>k-1</sub>, x<sub>k</sub>)]</i>, if {@code doub}
	 *         is true; <br>
	 *         <i>[R<sub>1</sub>(x, x<sub>1</sub>), ..., R<sub>k</sub> (x<sub>k-1</sub> , x<sub>k</sub>)]</i>, otherwise.
	 */
	protected List<Atom> tr(List<OWLObjectPropertyExpression> chain, Variable x, Variable xk, boolean doub) {
		final int n = chain.size();
		final List<Atom> result = new ArrayList<Atom>(n);
		Variable xi = x;
		Variable xj = x;
		for (int i = 0; i < n; i++) {
			final OWLProperty<?, ?> pe = (OWLProperty<?, ?>) chain.get(i);
			xi = xj;
			xj = i == n - 1 ? xk : var("X" + i);
			result.add(tr(pe, xi, xj, doub));
		}
		return result;
	}

	/**
	 * Translate an atomic concept to an atom according according to <b> Definition 12.</b> of {@link <a>A Correct EL Oracle for NoHR (Technical
	 * Report)</a>}. Corresponds to <i>tr(A, x)</i> and <i>tr(A, x)<sup>d</sup>.
	 *
	 * @param c
	 *            an atomic concept <i>A</i>.
	 * @param x
	 *            a variable <i>x</i>.
	 * @param doub
	 *            specifies whether the atom will be doubled (i.e. with an double meta-predicate functor).
	 * @return <i>A<sup>d</sup>(x)</i>, if {@code doub} is true; <br>
	 *         <i>A(x)</i>, otherwise.
	 */
	protected Atom tr(OWLClass c, Variable x, boolean doub) {
		final Predicate pred = Predicates.pred(c, doub);
		return atom(pred, x);
	}

	/**
	 * Translate a concept to a list of atoms according according to <b> Definition 12.</b> of {@link <a>A Correct EL Oracle for NoHR (Technical
	 * Report)</a>}. Corresponds to <i>tr(C, x)</i> and <i>tr(C, x)<sup>d</sup>.
	 *
	 * @param ce
	 *            an concept <i>C</i>.
	 * @param x
	 *            a variable <i>x</i>.
	 * @param doub
	 *            specifies whether the atom will be doubled (i.e. with an double meta-predicate functor).
	 * @return <i>{A(x)}</i> if <i>C</i> is an atomic concept <i>A</i>; <br>
	 *         <i>{}</i> if <i>C</i> is the top concept &#x22A5; <br>
	 *         <i>tr(C<sub>1</sub>, x) &cup; tr(C<sub>2</sub>, x) if <i>C</i> is an conjunction <i>C<sub>1</sub> &prod; <i>C<sub>2</sub></i>; </br>
	 *         <i>{R(x, x<sub>1</sub>)} &cup; tr(D, x<sub>1</sub>)</i> if <i>C</i> is an existential <i>&exist;R.D</i> <br>
	 *         ; and the corresponding double translation if {@code doub} is true.
	 */
	protected List<Literal> tr(OWLClassExpression ce, Variable x, boolean doub) {
		final List<Literal> result = new ArrayList<Literal>();
		if (ce.isOWLThing())
			return literalsList();
		else if (ce instanceof OWLClass && !ce.isOWLThing())
			return literalsList(tr(ce.asOWLClass(), x, doub));
		else if (ce instanceof OWLObjectIntersectionOf) {
			final Set<OWLClassExpression> ops = ce.asConjunctSet();
			for (final OWLClassExpression op : ops)
				result.addAll(tr(op, x, doub));
		} else if (ce instanceof OWLObjectSomeValuesFrom) {
			final OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) ce;
			final OWLProperty<?, ?> p = some.getProperty().asOWLObjectProperty();
			final OWLClassExpression filler = some.getFiller();
			result.add(tr(p, X, Y, doub));
			result.addAll(tr(filler, Y, doub));
		}
		return result;
	}

	/**
	 * Translate an atomic role to an atom according to <b> Definition 12.</b> of {@link <a>A Correct EL Oracle for NoHR (Technical Report)</a>}.
	 * Corresponds to <i>tr(R, x, x<sub>1</sub>)</i> and <i>tr(R, x, x<sub>1</sub>)<sup>d</sup></i>.
	 *
	 * @param r
	 *            a role <i>R</i>.
	 * @param x
	 *            a variable <i>x</i>.
	 * @param x1
	 *            a variable <i>x<sub>1</sub>.
	 * @param doub
	 *            specifies whether the returned axioms will be doubled (i.e. with a double meta-predicate functor).
	 * @return <i>R<sup>d</sup>(x, x<sub>1</sub>)</i>, if {@code doub} is true; <br>
	 *         <i>R(x, x<sub>1</sub></i>), otherwise.
	 */
	protected Atom tr(OWLProperty<?, ?> r, Variable x, Variable x1, boolean doub) {
		final Predicate pred = Predicates.pred(r, doub);
		if (r instanceof OWLProperty)
			return atom(pred, x, x1);
		return null;
	}

	/**
	 * Partially (depending on the concrete {@link ELAxiomsTranslator} used) translate a concept assertion to a set of rules according to <b>(a1)</b>
	 * of <b>Definition 13.</b> of {@link <a>A Correct EL Oracle for NoHR (Technical Report)</a>}.
	 *
	 * @param assertion
	 *            an assertion
	 */
	public abstract Set<Rule> translate(OWLClassAssertionAxiom assertion);

	/**
	 * Partially (depending on the concrete {@link ELAxiomsTranslator} used) translate a role assertion to a set of rules according to <b>(a2)</b> of
	 * <b>Definition 13.</b> of {@link <a>A Correct EL Oracle for NoHR (Technical Report)</a>}.
	 *
	 * @param assertion
	 *            an assertion
	 */
	public abstract Set<Rule> translate(OWLPropertyAssertionAxiom<?, ?> assertion);

	/**
	 * Partially (depending on the concrete {@link ELAxiomsTranslator} used) translate a concept subsumption axiom to a set of rules according to
	 * <b>(t1)</b>, <b>(c1)</b>, <b>(i1)</b> or <b>(i2)</b> of <b>Definition 13.</b>, and respective assumed simplifications, of {@link <a>A Correct
	 * EL Oracle for NoHR (Technical Report)</a>} .
	 *
	 * @param axiom
	 *            an axiom
	 */
	public Set<Rule> translate(OWLSubClassOfAxiom axiom) {
		final OWLClassExpression ce1 = axiom.getSubClass();
		final OWLClassExpression ce2 = axiom.getSuperClass();
		if (ce2.isAnonymous())
			return ruleSet();
		for (final OWLClassExpression ci : ce1.asConjunctSet())
			if (ci.isOWLNothing())
				return ruleSet();
		if (ce2.isOWLThing())
			return ruleSet();
		return translateSubsumption(ce1, (OWLClass) ce2);
	}

	/**
	 * Partially (depending on the concrete {@link ELAxiomsTranslator} used) translate a concept subsumption axiom to a set of rules according to
	 * <b>(r1)</b> of <b>Definition 13.</b>, and respective assumed simplifications, of {@link <a>A Correct EL Oracle for NoHR (Technical Report)</a>}
	 * .
	 *
	 * @param axiom
	 *            an axiom
	 */
	public Set<Rule> translate(OWLSubPropertyAxiom<?> axiom) {
		final OWLProperty<?, ?> pe1 = (OWLProperty<?, ?>) axiom.getSubProperty();
		final OWLProperty<?, ?> pe2 = (OWLProperty<?, ?>) axiom.getSuperProperty();
		return translateSubsumption(pe1, pe2);
	}

	/**
	 * Partially (depending on the concrete {@link ELAxiomsTranslator} used) translate a role chain subsumption axiom to a set of rules according to
	 * <b>(r2)</b> of <b>Definition 13.</b> of {@link <a>A Correct EL Oracle for NoHR (Technical Report)</a>} .
	 *
	 * @param axiom
	 *            an axiom
	 */
	public Set<Rule> translate(OWLSubPropertyChainOfAxiom axiom) {
		final List<OWLObjectPropertyExpression> chain = axiom.getPropertyChain();
		final OWLObjectPropertyExpression superProperty = axiom.getSuperProperty();
		return translateSubsumption(chain, (OWLObjectProperty) superProperty);
	}

	/**
	 * Partially (according to some {@link ELAxiomsTranslator} implementing class's criteria) translate a role chain subsumption axiom to a set of
	 * rules according to <b>(r2)</b> of <b>Definition 13.</b> of {@link <a>A Correct EL Oracle for NoHR (Technical Report)</a>} .
	 *
	 * @param chain
	 *            a role chain
	 * @param superRole
	 *            the subsuming role.
	 */
	protected abstract Set<Rule> translateSubsumption(List<OWLObjectPropertyExpression> chain,
			OWLObjectProperty superRole);

	/**
	 * Partially (according to some {@link ELAxiomsTranslator} implementing class's criteria) translate a concept subsumption axiom to a set of rules
	 * according to <b>(t1)</b>, <b>(c1)</b>, <b>(i1)</b> or <b>(i2)</b> of <b>Definition 13.</b> of {@link <a>A Correct EL Oracle for NoHR (Technical
	 * Report)</a>} .
	 *
	 * @param ce1
	 *            the subsumed concept expression
	 * @param c2
	 *            the subsuming concept.
	 */
	protected abstract Set<Rule> translateSubsumption(OWLClassExpression ce1, OWLClass c2);

	/**
	 * Partially (according to some {@link ELAxiomsTranslator} implementing class's criteria) translate a role subsumption axiom to a set of rules
	 * according to <b>(r1)</b> of <b>Definition 13.</b> of {@link <a>A Correct EL Oracle for NoHR (Technical Report)</a>} .
	 *
	 * @param r1
	 *            the subsumed role
	 * @param r2
	 *            the subsuming role.
	 */
	protected abstract Set<Rule> translateSubsumption(OWLProperty<?, ?> r1, OWLProperty<?, ?> r2);
}