package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.InvalidNanopublicationException;
import edu.rpi.tw.twdb.api.NamedModel;
import edu.rpi.tw.twdb.api.Nanopublication;
import edu.rpi.tw.twdb.api.NanopublicationFactory;
import edu.rpi.tw.twdb.lib.vocabulary.NANOPUB;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class NanopublicationFactoryImpl implements NanopublicationFactory {
    @Override
    public Nanopublication createNanopublicationFromDataset(final Dataset dataset) throws InvalidNanopublicationException {
        // All triples must be placed in one of [H] or [A] or [P] or [I]
        if (dataset.getDefaultModel().isEmpty()) {
            throw new InvalidNanopublicationException("default model is not empty");
        }

        final Set<String> datasetModelNames = new HashSet<>();
        dataset.listNames().forEachRemaining(modelName -> {
            if (datasetModelNames.contains(modelName)) {
                throw new IllegalStateException();
            }
            datasetModelNames.add(modelName);
        });

        if (datasetModelNames.size() > 4) {
            throw new InvalidNanopublicationException("dataset contains too many named graphs");
        }

        // Find the head graph.
        for (final String modelName : datasetModelNames) {
            final Model head;
            final String headUri;
            final Resource nanopublicationResource;
            {
                final Model model = dataset.getNamedModel(modelName);
                final List<Resource> nanopublicationResources = model.listSubjectsWithProperty(RDF.type, NANOPUB.Nanopublication).toList();
                switch (nanopublicationResources.size()) {
                    case 0:
                        continue;
                    case 1:
                        // Dropdown
                        break;
                    default:
                        // There is exactly one quad of the form '[N] rdf:type np:Nanopublication [H]', which identifies [N] as the nanopublication URI, and [H] as the head URI
                        throw new InvalidNanopublicationException(String.format("nanopublication head graph %s has more than one rdf:type Nanopublication", modelName));
                }
                head = model;
                headUri = modelName;
                nanopublicationResource = nanopublicationResources.get(0);
                if (nanopublicationResource.getURI() == null) {
                    throw new InvalidNanopublicationException("nanopublication resource is a blank node");
                }
            }
            // Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasAssertion [A] [H]', which identifies [A] as the assertion URI
            final NamedModel assertion = getNanopublicationPartFromDataset(dataset, head, nanopublicationResource, NANOPUB.hasAssertion);
            // Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasProvenance [P] [H]', which identifies [P] as the provenance URI
            final NamedModel provenance = getNanopublicationPartFromDataset(dataset, head, nanopublicationResource, NANOPUB.hasProvenance);
            // Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasPublicationInfo [I] [H]', which identifies [I] as the publication information URI
            final NamedModel publicationInfo = getNanopublicationPartFromDataset(dataset, head, nanopublicationResource, NANOPUB.hasPublicationInfo);

            // The URIs for [N], [H], [A], [P], [I] must all be different
            final Set<String> partUris = new HashSet<>(5);
            for (final String partUri : new String[]{nanopublicationResource.getURI(), headUri, assertion.getName(), provenance.getName(), publicationInfo.getName()}) {
                if (partUri.contains(partUri)) {
                    throw new InvalidNanopublicationException(String.format("duplicate part URI %s", partUri));
                }
                partUris.add(partUri);
            }

            // Triples in [P] have at least one reference to [A]
            if (!provenance.getModel().listStatements(ResourceFactory.createResource(assertion.getName()), null, (String) null).hasNext()) {
                throw new InvalidNanopublicationException("provenance does not reference assertion graph");
            }

            // Triples in [I] have at least one reference to [N]
            if (!publicationInfo.getModel().listStatements(nanopublicationResource, null, (String) null).hasNext()) {
                throw new InvalidNanopublicationException("publication info does not reference nanopublication");
            }

            return new NanopublicationImpl(assertion, provenance, publicationInfo, nanopublicationResource.getURI());
        }

        throw new InvalidNanopublicationException("unable to locate head graph by rdf:type Nanopublication statement");
    }

    /**
     * Get the named model in a dataset that correspond to part of a nanopublication e.g., the named assertion graph.
     * The dataset contains
     * <nanopublication URI> nanopub:hasAssertion <assertion graph URI> .
     * The same goes for nanopub:hasProvenance and nanopub:hasPublicationInfo.
     */
    private static NamedModel getNanopublicationPartFromDataset(final Dataset dataset, final Model head, final Resource nanopublicationResource, final Property partProperty) throws InvalidNanopublicationException {
        final List<RDFNode> partRdfNodes = head.listObjectsOfProperty(nanopublicationResource, partProperty).toList();

        switch (partRdfNodes.size()) {
            case 0:
                throw new InvalidNanopublicationException(String.format("nanopublication %s has no %s", nanopublicationResource, partProperty));
            case 1:
                break;
            default:
                throw new InvalidNanopublicationException(String.format("nanopublication %s has more than one %s", nanopublicationResource, partProperty));
        }

        final RDFNode partRdfNode = partRdfNodes.get(0);

        if (!(partRdfNode instanceof Resource)) {
            throw new InvalidNanopublicationException(String.format("nanopublication %s %s is not a resource", nanopublicationResource, partProperty));
        }

        final Resource partResource = (Resource) partRdfNode;

        if (partResource.getURI() == null) {
            throw new InvalidNanopublicationException(String.format("nanopublication %s %s is a blank node", nanopublicationResource, partProperty));
        }


        final String partModelName = partResource.toString();
        @Nullable final Model partModel = dataset.getNamedModel(partModelName);
        if (partModel == null) {
            throw new InvalidNanopublicationException(String.format("nanopublication %s %s refers to a missing named graph (%s)", nanopublicationResource, partProperty, partResource));
        }

        if (partModel.isEmpty()) {
            throw new InvalidNanopublicationException(String.format("nanopublication %s %s refers to an empty named graph (%s)", nanopublicationResource, partProperty, partResource));
        }

        return new NamedModel(partModel, partModelName);
    }

}
