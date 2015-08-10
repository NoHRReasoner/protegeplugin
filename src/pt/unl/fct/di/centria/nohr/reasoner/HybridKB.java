/*
 *
 */
package pt.unl.fct.di.centria.nohr.reasoner;

import static pt.unl.fct.di.centria.nohr.model.Model.atom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.declarativa.interprolog.util.IPException;

import pt.unl.fct.di.centria.nohr.model.Answer;
import pt.unl.fct.di.centria.nohr.model.FormatVisitor;
import pt.unl.fct.di.centria.nohr.model.Literal;
import pt.unl.fct.di.centria.nohr.model.Query;
import pt.unl.fct.di.centria.nohr.model.Rule;
import pt.unl.fct.di.centria.nohr.model.ModelVisitor;
import pt.unl.fct.di.centria.nohr.model.predicates.NegativePredicate;
import pt.unl.fct.di.centria.nohr.model.predicates.Predicate;
import pt.unl.fct.di.centria.nohr.reasoner.translation.RulesDuplication;
import pt.unl.fct.di.centria.nohr.reasoner.translation.UnquoteVisitor;
import pt.unl.fct.di.centria.nohr.reasoner.translation.ontology.AbstractOntologyTranslation;
import pt.unl.fct.di.centria.nohr.reasoner.translation.ontology.OntologyTranslation;
import pt.unl.fct.di.centria.nohr.xsb.XSBDatabase;
import pt.unl.fct.di.centria.nohr.xsb.XSBDatabaseCreationException;
import pt.unl.fct.di.centria.nohr.xsb.XSBFormatVisitor;
import pt.unl.fct.di.centria.runtimeslogger.RuntimesLogger;

public class HybridKB {

    private static final String TRANSLATION_FILE_NAME = "nohrtr.P";

    private boolean hasDisjunctions;

    private boolean hasOntologyChanges;

    private final OWLOntology ontology;

    private OntologyTranslation ontologyTranslation;

    private final QueryProcessor queryProcessor;

    private final RuleBase ruleBase;

    private final Set<Rule> rulesDuplication;

    private final XSBDatabase xsbDatabase;

    public HybridKB(final File xsbBinDirectory) throws OWLProfilesViolationsException, IOException,
	    UnsupportedAxiomsException, IPException, XSBDatabaseCreationException {
	this(xsbBinDirectory, Collections.<OWLAxiom> emptySet());
    }

    public HybridKB(final File xsbBinDirectory, final RuleBase ruleBase) throws IOException,
	    OWLProfilesViolationsException, UnsupportedAxiomsException, IPException, XSBDatabaseCreationException {
	this(xsbBinDirectory, Collections.<OWLAxiom> emptySet(), new RuleBase());
    }

    public HybridKB(final File xsbBinDirectory, final Set<OWLAxiom> axioms) throws IOException,
	    OWLProfilesViolationsException, UnsupportedAxiomsException, IPException, XSBDatabaseCreationException {
	this(xsbBinDirectory, axioms, new RuleBase());
    }

    public HybridKB(final File xsbBinDirectory, final Set<OWLAxiom> axioms, final RuleBase ruleBase)
	    throws OWLProfilesViolationsException, UnsupportedAxiomsException, IPException, IOException,
	    XSBDatabaseCreationException {
	Objects.requireNonNull(xsbBinDirectory);
	try {
	    ontology = OWLManager.createOWLOntologyManager().createOntology(axioms);
	} catch (final OWLOntologyCreationException e) {
	    throw new RuntimeException(e);
	}
	hasOntologyChanges = true;
	xsbDatabase = new XSBDatabase(xsbBinDirectory);
	queryProcessor = new QueryProcessor(xsbDatabase);
	this.ruleBase = ruleBase;
	rulesDuplication = new HashSet<Rule>();
	preprocess();
    }

    public boolean addAxiom(OWLAxiom axiom) {
	final List<OWLOntologyChange> changes = ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
	if (!changes.isEmpty()) {
	    hasOntologyChanges = true;
	    return true;
	}
	return false;
    }

    public void dispose() {
	xsbDatabase.dispose();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
	super.finalize();
	dispose();
    }

    private File generateTranslationFile() throws IOException {
	final File file = FileSystems.getDefault().getPath(TRANSLATION_FILE_NAME).toAbsolutePath().toFile();
	final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	final Set<Predicate> tabled = new HashSet<Predicate>();
	tabled.addAll(ontologyTranslation.getTabledPredicates());
	tabled.addAll(rulesTabledPredicates());
	final FormatVisitor xsbFormatedVisitor = new XSBFormatVisitor();
	for (final Predicate predicate : tabled) {
	    writer.write(":- table " + predicate.accept(xsbFormatedVisitor) + "/" + predicate.getArity()
		    + " as subsumptive.");
	    writer.newLine();
	    if (predicate instanceof NegativePredicate
		    && !ontologyTranslation.getNegativeHeadsPredicates().contains(predicate)) {
		writer.write(atom(predicate).accept(xsbFormatedVisitor) + " :- fail.");
		writer.newLine();
	    }
	}
	for (final Rule rule : ontologyTranslation.getTranslation()) {
	    writer.write(rule.accept(xsbFormatedVisitor));
	    writer.newLine();
	}
	for (final Rule rule : rulesDuplication) {
	    writer.write(rule.accept(xsbFormatedVisitor));
	    writer.newLine();
	}
	writer.close();
	return file;
    }

    /**
     * @return the ruleBase
     */
    public RuleBase getRuleBase() {
	return ruleBase;
    }

    public boolean hasAnswer(Query query, boolean trueAnswer, boolean undefinedAnswers, boolean inconsistentAnswers)
	    throws OWLOntologyCreationException, OWLOntologyStorageException, OWLProfilesViolationsException,
	    IOException, CloneNotSupportedException, UnsupportedAxiomsException {
	if (hasOntologyChanges || ruleBase.hasChanges())
	    preprocess();
	RuntimesLogger.start("query");
	final boolean hasAnswer = queryProcessor.hasAnswer(query, hasDisjunctions, trueAnswer, undefinedAnswers,
		hasDisjunctions ? inconsistentAnswers : false);
	RuntimesLogger.stop("query", "queries");
	return hasAnswer;
    }

    private void preprocess() throws IOException, OWLProfilesViolationsException, UnsupportedAxiomsException {
	if (hasOntologyChanges) {
	    RuntimesLogger.start("ontology processing");
	    ontologyTranslation = AbstractOntologyTranslation.createOntologyTranslation(ontology);
	    RuntimesLogger.stop("ontology processing", "loading");
	}
	if (ruleBase.hasChanges(true) || ontologyTranslation.hasDisjunctions() != hasDisjunctions) {
	    RuntimesLogger.start("rules parsing");
	    rulesDuplication.clear();
	    for (final Rule rule : ruleBase.getRules())
		Collections.addAll(rulesDuplication, RulesDuplication.duplicate(rule, true));
	    RuntimesLogger.stop("rules parsing", "loading");
	}
	RuntimesLogger.start("file writing");
	final File xsbFile = generateTranslationFile();
	RuntimesLogger.stop("file writing", "loading");
	RuntimesLogger.start("xsb loading");
	xsbDatabase.clear();
	xsbDatabase.load(xsbFile);
	RuntimesLogger.stop("xsb loading", "loading");
	hasOntologyChanges = false;
	hasDisjunctions = ontologyTranslation.hasDisjunctions();
    }

    public Answer query(Query query) throws OWLOntologyCreationException, OWLOntologyStorageException,
	    OWLProfilesViolationsException, IOException, CloneNotSupportedException, UnsupportedAxiomsException {
	return query(query, true, true, true);
    }

    public Answer query(Query query, boolean trueAnswer, boolean undefinedAnswers, boolean inconsistentAnswers)
	    throws OWLOntologyCreationException, OWLOntologyStorageException, OWLProfilesViolationsException,
	    IOException, CloneNotSupportedException, UnsupportedAxiomsException {
	if (hasOntologyChanges || ruleBase.hasChanges())
	    preprocess();
	RuntimesLogger.start("query");

	final Answer answer = queryProcessor.query(query, hasDisjunctions, trueAnswer, undefinedAnswers,
		hasDisjunctions ? inconsistentAnswers : false);
	RuntimesLogger.stop("query", "queries");
	return answer.acept(new UnquoteVisitor());
    }

    public List<Answer> queryAll(Query query)
	    throws OWLProfilesViolationsException, UnsupportedAxiomsException, IOException {
	return queryAll(query, true, true, true);
    }

    public List<Answer> queryAll(Query query, boolean trueAnswer, boolean undefinedAnswers, boolean inconsistentAnswers)
	    throws IOException, OWLProfilesViolationsException, UnsupportedAxiomsException {
	if (hasOntologyChanges || ruleBase.hasChanges())
	    preprocess();
	RuntimesLogger.start("query");
	RuntimesLogger.info("querying: " + query);
	final List<Answer> answers = queryProcessor.queryAll(query, hasDisjunctions, trueAnswer, undefinedAnswers,
		hasDisjunctions ? inconsistentAnswers : false);
	RuntimesLogger.stop("query", "queries");
	final List<Answer> result = new LinkedList<Answer>();
	final ModelVisitor unquoteVisitor = new UnquoteVisitor();
	for (final Answer ans : answers)
	    result.add(ans.acept(unquoteVisitor));
	return result;
    }

    public boolean removeAxiom(OWLAxiom axiom) {
	final List<OWLOntologyChange> changes = ontology.getOWLOntologyManager().removeAxiom(ontology, axiom);
	if (!changes.isEmpty())
	    hasOntologyChanges = true;
	return !changes.isEmpty();
    }

    private Set<Predicate> rulesTabledPredicates() {
	final Set<Predicate> result = new HashSet<Predicate>();
	for (final Rule rule : rulesDuplication)
	    for (final Literal literal : rule.getNegativeBody())
		result.add(literal.getFunctor());
	return result;
    }

}
