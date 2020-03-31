package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.nanopub.NanopublicationConsumer;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

/**
 * A TwksTransaction that wraps a DatasetTransaction.
 */
public abstract class DatasetTwksTransaction<TwksT extends DatasetTwks<?>> extends QuadStoreTwksTransaction<TwksT> {
    private final Dataset dataset;

    protected DatasetTwksTransaction(final ReadWrite readWrite, final TwksT twks) {
        super(new DatasetQuadStoreTransaction(twks.getDataset(), new DatasetTransaction(twks.getDataset(), readWrite)), twks);
        this.dataset = twks.getDataset();
    }

    protected final Dataset getDataset() {
        return dataset;
    }

    @Override
    protected final void getNanopublications(final NanopublicationConsumer consumer) {
        NanopublicationParser.DEFAULT.parseDataset(dataset, consumer);
    }

    @Override
    public final boolean isEmpty() {
        return dataset.isEmpty();
    }
}

