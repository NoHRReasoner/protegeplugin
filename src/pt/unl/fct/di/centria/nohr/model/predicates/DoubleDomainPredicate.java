/**
 *
 */
package pt.unl.fct.di.centria.nohr.model.predicates;

/**
 * @author nunocosta
 *
 */
public class DoubleDomainPredicate extends MetaPredicateImpl {

    public DoubleDomainPredicate(String symbol) {
	super(symbol, 1, PredicateType.DOUBLE_DOMAIN, 'g');
    }

}