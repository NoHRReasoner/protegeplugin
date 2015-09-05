package pt.unl.fct.di.novalincs.nohr.model;

import pt.unl.fct.di.novalincs.nohr.model.vocabulary.ModelVisitor;

/**
 * Represents a rule term. Can be a variable, a constant, or a list.
 *
 * @see Variable
 * @see Constant
 * @author Nuno Costa
 */
public interface Term extends Symbol {

	@Override
	Term accept(ModelVisitor visitor);
}