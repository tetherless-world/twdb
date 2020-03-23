package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationParser {
    private final NanopublicationDialect dialect;
    private final RDFParser rdfParser;
    private final Optional<Uri> sourceUri;

    NanopublicationParser(final NanopublicationDialect dialect, final RDFParser rdfParser, final Optional<Uri> sourceUri) {
        this.dialect = checkNotNull(dialect);
        this.rdfParser = checkNotNull(rdfParser);
        this.sourceUri = checkNotNull(sourceUri);
    }

    public final static NanopublicationParserBuilder builder() {
        return new NanopublicationParserBuilder();
    }

    public final ImmutableList<Nanopublication> parseAll() throws MalformedNanopublicationException {
        final Dataset dataset = DatasetFactory.create();

        try {
            rdfParser.parse(dataset);
        } catch (final RiotNotFoundException e) {
            throw e;
        } catch (final RiotException e) {
            throw new MalformedNanopublicationException(e);
        }

        // Dataset has named graphs, assume it's a well-formed nanopublication.
        final boolean datasetHasNamedGraphs;
        try {
            datasetHasNamedGraphs = dataset.listNames().hasNext();
        } catch (final UnsupportedOperationException e) {
            // Jena throws this exception when a graph name is a blank node
            // The latter appears to be legal TriG.
            throw new MalformedNanopublicationException("blank node graph names not supported");
        }
        if (datasetHasNamedGraphs) {
            return DatasetNanopublications.copyAll(dataset, dialect);
        }

        final NanopublicationBuilder nanopublicationBuilder = Nanopublication.builder();
        nanopublicationBuilder.getAssertionBuilder().setModel(dataset.getDefaultModel());
        if (sourceUri.isPresent()) {
            nanopublicationBuilder.getProvenanceBuilder().wasDerivedFrom(sourceUri.get());
        }
        return ImmutableList.of(nanopublicationBuilder.build());
    }

    public final Nanopublication parseOne() throws MalformedNanopublicationException {
        final ImmutableList<Nanopublication> nanopublications = parseAll();

        switch (nanopublications.size()) {
            case 0:
                throw new IllegalStateException();
            case 1:
                return nanopublications.get(0);
            default:
                throw new MalformedNanopublicationException("more than one nanopublication parsed");
        }
    }
}
