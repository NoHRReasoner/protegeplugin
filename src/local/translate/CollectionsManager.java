package local.translate;

import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * The Class CollectionsManager.
 */
public class CollectionsManager {

    /** The table predicates ontology. */
    private HashSet<String> tablePredicatesOntology = new HashSet<String>();

    /** The table predicates rules. */
    private HashSet<String> tablePredicatesRules = new HashSet<String>();

    /** The translated ontologies. */
    private HashSet<String> translatedOntologies = new HashSet<String>();

    /** The appended rules. */
    private HashSet<String> appendedRules = new HashSet<String>();

    /** The prediactes appeared under nunderscore. */
    private HashSet<String> prediactesAppearedUnderNunderscore = new HashSet<String>();

    /** The labels. */
    private final HashMap<String, String> labels = new HashMap<String, String>();

    /** The labels value. */
    private final HashMap<String, String> labelsValue = new HashMap<String, String>();

    /** The Constant log. */
    private static final Logger log = Logger.getLogger(Translate.class);

    /** The is any disjoint statement. */
    private boolean isAnyDisjointStatement;

    /**
     * Instantiates a new collections manager.
     */
    public CollectionsManager() {
	isAnyDisjointStatement = false;
	log.setLevel(Config.logLevel);
    }

    /**
     * Adds the prediactes appeared under nunderscore.
     * 
     * @param predicate
     *            the predicate
     */
    public void addPrediactesAppearedUnderNunderscore(String predicate) {
	prediactesAppearedUnderNunderscore.add(predicate);
    }

    /**
     * Adds the tabled predicate ontology.
     * 
     * @param title
     *            the title
     */
    public void addTabledPredicateOntology(String title) {
	tablePredicatesOntology.add(title);
    }

    /**
     * Adds the tabled predicate rule.
     * 
     * @param title
     *            the title
     */
    public void addTabledPredicateRule(String title) {
	tablePredicatesRules.add(title);
    }

    /**
     * Adds the translated ontology.
     * 
     * @param rule
     *            the rule
     */
    public void addTranslatedOntology(String rule) {
	translatedOntologies.add(rule);
    }

    /**
     * Adds the translated rule.
     * 
     * @param rule
     *            the rule
     */
    public void addTranslatedRule(String rule) {
	appendedRules.add(rule);
    }

    /**
     * Clear.
     */
    public void clear() {
	tablePredicatesOntology = new HashSet<String>();
	tablePredicatesRules = new HashSet<String>();
	translatedOntologies = new HashSet<String>();
	appendedRules = new HashSet<String>();
	prediactesAppearedUnderNunderscore = new HashSet<String>();
    }

    /**
     * Clear ontology.
     */
    public void clearOntology() {
	log.info("Clearing ontology set");
	tablePredicatesOntology = new HashSet<String>();
	translatedOntologies = new HashSet<String>();
	prediactesAppearedUnderNunderscore = new HashSet<String>();
    }

    /**
     * Clear rules.
     */
    public void clearRules() {
	log.info("Clearing rules set");
	tablePredicatesRules = new HashSet<String>();
	appendedRules = new HashSet<String>();
    }

    /**
     * Gets the all tabled predicate ontology.
     * 
     * @return the all tabled predicate ontology
     */
    public HashSet<String> getAllTabledPredicateOntology() {
	return tablePredicatesOntology;
    }

    /**
     * Gets the all tabled predicate rule.
     * 
     * @return the all tabled predicate rule
     */
    public HashSet<String> getAllTabledPredicateRule() {
	return tablePredicatesRules;
    }

    /**
     * Gets the hashed label.
     * 
     * @param label
     *            the label
     * @return the hashed label
     */
    public String getHashedLabel(String label) {
	if (labelsValue.containsKey(label)) {
	    return labelsValue.get(label);
	} else {
	    String hash = Utils.getHash(label);
	    labels.put(hash, label);
	    labelsValue.put(label, hash);
	    return hash;
	}
    }

    /**
     * Gets the label by hash.
     * 
     * @param hash
     *            the hash
     * @return the label by hash
     */
    public String getLabelByHash(String hash) {
	String originalHash = hash;
	hash = hash.substring(1, hash.length());
	if (labels.containsKey(hash)) {
	    return labels.get(hash);
	}
	return originalHash;
    }

    /**
     * Gets the translated ontologies.
     * 
     * @return the translated ontologies
     */
    public HashSet<String> getTranslatedOntologies() {
	return translatedOntologies;
    }

    /**
     * Gets the translated rules.
     * 
     * @return the translated rules
     */
    public HashSet<String> getTranslatedRules() {
	return appendedRules;
    }

    /**
     * Checks if is any disjoint statement.
     * 
     * @return true, if is any disjoint statement
     */
    public boolean isAnyDisjointStatement() {
	return isAnyDisjointStatement;
    }

    /**
     * Checks if is prediactes appeared under nunderscore.
     * 
     * @param predicate
     *            the predicate
     * @return true, if is prediactes appeared under nunderscore
     */
    public boolean isPrediactesAppearedUnderNunderscore(String predicate) {
	return prediactesAppearedUnderNunderscore.contains(predicate);
    }

    /**
     * Checks if is tabled.
     * 
     * @param title
     *            the title
     * @return true, if is tabled
     */
    public boolean isTabled(String title) {
	return tablePredicatesOntology.contains(title)
		|| tablePredicatesRules.contains(title);
    }

    /**
     * Prints the all labels.
     */
    public void printAllLabels() {

	FileWriter writer = null;
	try {
	    writer = new FileWriter("labels.txt");
	    Iterator it = labels.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry pairs = (Map.Entry) it.next();
		writer.write(pairs.getKey() + " = " + pairs.getValue() + "\n");
		// it.remove(); // avoids a ConcurrentModificationException
	    }
	    writer.close();

	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    /**
     * Sets the checks if is any disjoint statement.
     * 
     * @param value
     *            the new checks if is any disjoint statement
     */
    public void setIsAnyDisjointStatement(boolean value) {
	isAnyDisjointStatement = value;
    }
}
