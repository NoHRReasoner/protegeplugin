/**
 *
 */
package pt.unl.fct.di.centria.nohr.model.predicates;

/**
 * @author nunocosta
 *
 */
public class OriginalPredicate extends MetaPredicateImpl {

    public OriginalPredicate(String symbol, int arity) {
	super(symbol, arity, PredicateType.ORIGINAL, 'a');
    }

}