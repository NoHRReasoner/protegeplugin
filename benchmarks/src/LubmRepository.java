import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import other.Config;
import pt.unl.fct.di.centria.nohr.model.Answer;
import pt.unl.fct.di.centria.nohr.model.Term;
import pt.unl.fct.di.centria.nohr.parsing.Parser;
import pt.unl.fct.di.centria.nohr.plugin.Rules;
import pt.unl.fct.di.centria.nohr.reasoner.HybridKB;
import pt.unl.fct.di.centria.nohr.reasoner.translation.ontology.TranslationAlgorithm;
import ubt.api.Query;
import ubt.api.QueryResult;
import utils.Tracer;

import com.igormaznitsa.prologparser.exceptions.PrologParserException;

public class LubmRepository {

    private pt.unl.fct.di.centria.nohr.reasoner.HybridKB nohrQuery;

    private String lastQuery;

    private IRI mainOntologyIRI;

    private String dataset;

    private int queryCount;

    private File ontology;

    private Path data;

    private String resultsDirectory;

    private final Parser parser = new Parser();

    int universities = 0;

    public LubmRepository() {
	queryCount = 1;
    }

    public LubmRepository(Path data, String resultsDirectory) {
	this.resultsDirectory = resultsDirectory;
	queryCount = 1;
	this.data = data;
    }

    public void clear() {
	nohrQuery = null;
    }

    public void close() {
	nohrQuery = null;
	mainOntologyIRI = null;
	lastQuery = null;
    }

    public QueryResult issueQuery(Query query) throws IOException,
	    PrologParserException, Exception {
	String queryStr = query.getString();
	boolean sameQuery = queryStr.equals(lastQuery);
	String[] queryStruct = queryStr.split(":-");
	if (queryStruct.length == 2 && !queryStr.equals(lastQuery)) {
	    Rules.addRule(query.getString());
	    System.out.println("Added rule: " + queryStr);
	}
	if (!sameQuery)
	    nohrQuery.abolishTables();
	String q = queryStruct[0];
	if (!q.endsWith("."))
	    q = q + ".";
	Collection<Answer> result = nohrQuery.queryAll(parser.parseQuery(q));
	Tracer.info(String.valueOf(result.size()) + " answers");
	if (!sameQuery && resultsDirectory != null)
	    logResults(query.getString(), result);
	lastQuery = queryStr;
	if (!sameQuery)
	    queryCount++;
	return new NoHRQueryResult(result);
    }

    public boolean load(int universities) throws OWLOntologyCreationException {
	this.universities = universities;
	OWLOntologyManager inManager = OWLManager.createOWLOntologyManager();
	OWLOntologyManager outManager = OWLManager.createOWLOntologyManager();

	Tracer.start("ontology loading");
	for (int u = 0; u < universities; u++)
	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(data,
		    "University" + u + "_*.owl")) {
		for (Path entry : stream) {
		    Tracer.info("loading " + entry.getFileName().toString());
		    if (entry.getFileName().toString().endsWith(".owl")) {
			File file = entry.toFile();
			inManager.loadOntologyFromOntologyDocument(file);
		    }
		}
	    } catch (IOException x) {
		System.err.println(x);
		return false;
	    } catch (OWLOntologyCreationException e) {
		PrintStream s;
		try {
		    s = new PrintStream("log");
		    e.printStackTrace(s);
		    s.close();
		} catch (FileNotFoundException e1) {
		    e1.printStackTrace();
		}
		e.printStackTrace();
		return false;
	    }
	Tracer.stop("ontology loading", "loading");
	OWLOntology outOntology = outManager.createOntology(
		IRI.generateDocumentIRI(), inManager.getOntologies(), true);
	inManager = null;
	outManager = null;
	nohrQuery = new HybridKB(outOntology);
	outOntology = null;
	System.gc();
	return true;

    }

    private void loadRules(Path path) throws IOException {
	File file = path.toFile();
	if (file.exists()) {
	    FileInputStream fstream = new FileInputStream(file);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    // Read File Line By Line
	    while ((strLine = br.readLine()) != null)
		if (strLine.length() > 0)
		    Rules.addRule(strLine);
	    in.close();

	}
	System.out.println("Additional rule file: " + file.getName());
	// ontology.setResultFileName(file.getName());
	file = null;
    }

    private void logResults(String query, Collection<Answer> result2) {
	// if (!dataset.endsWith("/1"))
	// return;
	Charset charset = Charset.defaultCharset();
	String fileName = universities + "." + queryCount + ".txt";
	Path path = FileSystems.getDefault()
		.getPath(resultsDirectory, fileName);
	try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
	    if (result2.size() >= 1)
		for (Answer result : result2) {
		    for (Term val : result.getValues()) {
			String valStr = val.toString();
			writer.write(valStr, 0, valStr.length());
			writer.write(9);
		    }
		    writer.newLine();
		}
	} catch (IOException x) {
	    System.err.format("IOException: %s%n", x);
	}
    }

    public void open(String database) {
	Config.translationAlgorithm = TranslationAlgorithm.DL_LITE_R;
    }

    public void setOntology(String ontology) {
	Path path = FileSystems.getDefault().getPath(ontology).toAbsolutePath();
	this.ontology = new File(path.toString());
	// mainOntologyIRI = IRI.create(new File(path.toString()));
    }

}