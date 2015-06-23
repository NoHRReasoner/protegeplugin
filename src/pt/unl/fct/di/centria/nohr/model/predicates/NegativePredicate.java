/**
 *
 */
package pt.unl.fct.di.centria.nohr.model.predicates;

/**
 * @author nunocosta
 *
 */
public class NegativePredicate extends MetaPredicateImpl {

    public NegativePredicate(String symbol, int arity) {
	super(symbol, arity, PredicateType.NEGATIVE, 'n');
    }

}
