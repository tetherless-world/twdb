package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.nanopub.*;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

public abstract class AbstractTwksTransaction<TwksT extends Twks> implements TwksTransaction {
    private final static String GET_ASSERTION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "select ?A where {\n" +
            "  graph ?H { ?NP np:hasAssertion ?A }\n" +
            "  graph ?A {?S ?P ?O}\n" +
            "}";

    private final static Query GET_ASSERTION_GRAPH_NAMES_QUERY = QueryFactory.create(GET_ASSERTION_GRAPH_NAMES_QUERY_STRING);
    private final static String GET_NANOPUBLICATION_DATASET_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G ?S ?P ?O where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";
    private final static String GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";
    private final static String GET_ONTOLOGY_ASSERTION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix sio: <http://semanticscience.org/resource/>\n" +
            "select ?A where {\n" +
            "  graph ?H { ?NP a np:Nanopublication . ?NP np:hasAssertion ?A . ?NP np:hasPublicationInfo ?I . }\n" +
            "  graph ?I { ?NP sio:isAbout <%s> }\n" +
            "  graph ?A {?S ?P ?O}\n" +
            "}";

    private final static Logger logger = LoggerFactory.getLogger(AbstractTwksTransaction.class);
    private final TwksT twks;

    protected AbstractTwksTransaction(final TwksT twks) {
        this.twks = checkNotNull(twks);
    }

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        final Set<String> nanopublicationGraphNames = getNanopublicationGraphNames(uri);
        if (nanopublicationGraphNames.isEmpty()) {
            return DeleteNanopublicationResult.NOT_FOUND;
        }
        if (nanopublicationGraphNames.size() != 4) {
            throw new IllegalStateException();
        }
        deleteNanopublication(nanopublicationGraphNames);
        return DeleteNanopublicationResult.DELETED;
    }

    protected abstract void deleteNanopublication(final Set<String> nanopublicationGraphNames);

    @Override
    public final ImmutableList<DeleteNanopublicationResult> deleteNanopublications(final ImmutableList<Uri> uris) {
        return uris.stream().map(uri -> deleteNanopublication(uri)).collect(ImmutableList.toImmutableList());
    }

    @Override
    public final void dump() throws IOException {
        final Path dumpDirectoryPath = twks.getConfiguration().getDumpDirectoryPath();
        if (!Files.isDirectory(dumpDirectoryPath)) {
            logger.info("dump directory {} does not exist, creating", dumpDirectoryPath);
            Files.createDirectory(dumpDirectoryPath);
            logger.info("created dump directory {}", dumpDirectoryPath);
        }

        final Map<String, Uri> nanopublicationFileNames = new HashMap<>();
        try {
            try (final AutoCloseableIterable<Nanopublication> nanopublications = iterateNanopublications()) {
                for (final Nanopublication nanopublication : nanopublications) {
                    final String nanopublicationFileName = MoreFilenameUtils.cleanFileName(nanopublication.getUri().toString()) + ".trig";

                    {
                        @Nullable final Uri conflictNanopublicationUri = nanopublicationFileNames.get(nanopublicationFileName);
                        if (conflictNanopublicationUri != null) {
                            throw new IllegalStateException(String.format("duplicate nanopublication file name: %s (from URIs %s and %s)", nanopublicationFileName, nanopublication.getUri(), conflictNanopublicationUri));
                        }
                    }

                    nanopublicationFileNames.put(nanopublicationFileName, nanopublication.getUri());

                    final Path dumpFilePath = dumpDirectoryPath.resolve(nanopublicationFileName);
                    try (final FileOutputStream fileOutputStream = new FileOutputStream(dumpFilePath.toFile())) {
                        nanopublication.write(fileOutputStream);
                        logger.debug("wrote {} to {}", nanopublication.getUri(), dumpFilePath);
                    }
                }
            }
        } catch (final MalformedNanopublicationRuntimeException e) {
            logger.error("malformed nanopublication: ", e);
        }
    }

    protected final Set<String> getAssertionGraphNames() {
        final Set<String> assertionGraphNames = new HashSet<>();
        try (final QueryExecution queryExecution = queryNanopublications(GET_ASSERTION_GRAPH_NAMES_QUERY)) {
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("A");
                assertionGraphNames.add(g.getURI());
            }
        }
        return assertionGraphNames;
    }

    @Override
    public final Model getAssertions() {
        return getAssertions(getAssertionGraphNames());
    }

    private final Model getAssertions(final Set<String> assertionGraphNames) {
        final Model assertions = ModelFactory.createDefaultModel();
        if (assertionGraphNames.isEmpty()) {
            return assertions;
        }
        setNsPrefixes(assertions);
        getAssertions(assertionGraphNames, assertions);
        return assertions;
    }

    protected abstract void getAssertions(Set<String> assertionGraphNames, Model assertions);

    @Override
    public final Optional<Nanopublication> getNanopublication(final Uri uri) {
        final Dataset nanopublicationDataset = getNanopublicationDataset(uri);
        if (nanopublicationDataset.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(DatasetNanopublications.copyOne(nanopublicationDataset));
        } catch (final MalformedNanopublicationException e) {
            throw new IllegalStateException(e);
        }
    }

    protected final Dataset getNanopublicationDataset(final Uri uri) {
        try (final QueryExecution queryExecution = queryNanopublications(QueryFactory.create(String.format(GET_NANOPUBLICATION_DATASET_QUERY_STRING, uri)))) {
            return MoreDatasetFactory.createDatasetFromResultSet(queryExecution.execSelect());
        }
    }

    protected final Set<String> getNanopublicationGraphNames(final Uri nanopublicationUri) {
        final Set<String> nanopublicationGraphNames = new HashSet<>();
        try (final QueryExecution queryExecution = queryNanopublications(QueryFactory.create(String.format(GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING, nanopublicationUri)))) {
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("G");
                nanopublicationGraphNames.add(g.getURI());
            }
        }
        return nanopublicationGraphNames;
    }

    protected final Set<String> getOntologyAssertionGraphNames(final ImmutableSet<Uri> ontologyUris) {
        final Set<String> assertionGraphNames = new HashSet<>();
        for (final Uri ontologyUri : ontologyUris) {
            try (final QueryExecution queryExecution = queryNanopublications(QueryFactory.create(String.format(GET_ONTOLOGY_ASSERTION_GRAPH_NAMES_QUERY_STRING, ontologyUri)))) {
                for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                    final QuerySolution querySolution = resultSet.nextSolution();
                    final Resource g = querySolution.getResource("A");
                    assertionGraphNames.add(g.getURI());
                }
            }
        }
        return assertionGraphNames;
    }

    @Override
    public final Model getOntologyAssertions(final ImmutableSet<Uri> ontologyUris) {
        return getAssertions(getOntologyAssertionGraphNames(ontologyUris));
    }

    @Override
    public final TwksT getTwks() {
        return twks;
    }

    protected abstract AutoCloseableIterable<Nanopublication> iterateNanopublications();

    @Override
    public final ImmutableList<PutNanopublicationResult> postNanopublications(final ImmutableList<Nanopublication> nanopublications) {
        return nanopublications.stream().map(nanopublication -> putNanopublication(nanopublication)).collect(ImmutableList.toImmutableList());
    }

    @Override
    public final QueryExecution queryAssertions(final Query query) {
        // https://jena.apache.org/documentation/tdb/dynamic_datasets.html
        // Using one or more FROM clauses, causes the default graph of the dataset to be the union of those graphs.
        final Set<String> assertionGraphNames = getAssertionGraphNames();
        if (assertionGraphNames.isEmpty()) {
            logger.warn("no assertion graph names, querying empty model");
            return QueryExecutionFactory.create(query, ModelFactory.createDefaultModel());
        }
        for (final String assertionGraphName : assertionGraphNames) {
            query.addGraphURI(assertionGraphName);
        }
        return queryNanopublications(query);
    }
}
