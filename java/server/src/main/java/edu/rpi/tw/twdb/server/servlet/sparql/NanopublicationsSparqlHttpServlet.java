package edu.rpi.tw.twdb.server.servlet.sparql;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.api.TwdbTransaction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

@Singleton
class NanopublicationsSparqlHttpServlet extends SparqlHttpServlet {
    @Inject
    NanopublicationsSparqlHttpServlet(final Twdb db) {
        super(db);
    }

    @Override
    protected final QueryExecution query(final Query query, final TwdbTransaction transaction) {
        return getDb().queryNanopublications(query, transaction);
    }
}
