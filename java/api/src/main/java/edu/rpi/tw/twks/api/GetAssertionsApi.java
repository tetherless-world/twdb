package edu.rpi.tw.twks.api;

import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.rdf.model.Model;

public interface GetAssertionsApi {
    /**
     * Get a Model that contains the union of all assertion parts of stored nanopublications.
     * <p>
     * Writes to the returned Model will not be reflected in the underlying store.
     * <p>
     * The combining rule for assertions from different nanopublications is currently (201910) unspecified.
     *
     * @return Model containing assertions about the given ontologies
     */
    Model getAssertions();

    /**
     * Get a Model that contains the onion of all assertions parts of stored nanopublications that assert about the given ontology URIs.
     * <p>
     * A nanopublication is considered about an ontology if it contains an assertion ?x a owl:Ontology.
     * <p>
     * Writes to the returned Model will not be reflected in the underlying store.
     * <p>
     * The combining rule for assertions from different nanopublications is currently (201910) unspecified.
     *
     * @param ontologyUris set of ontology URIs (?x)
     * @return Model containing assertions about the given ontologies
     */
    Model getOntologyAssertions(ImmutableSet<Uri> ontologyUris);
}
