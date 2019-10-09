package edu.rpi.tw.twdb.server.servlet;

import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.server.AcceptLists;
import edu.rpi.tw.twdb.server.ServletContextTwdb;
import org.apache.jena.atlas.web.AcceptList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TwdbHttpServlet extends HttpServlet {
    private final Twdb db;

    protected TwdbHttpServlet() {
        this(ServletContextTwdb.getInstance());
    }

    protected TwdbHttpServlet(final Twdb db) {
        this.db = checkNotNull(db);
    }

    public final static Optional<AcceptList> getProposeAcceptList(final HttpServletRequest req) {
        return AcceptLists.getProposeAcceptList(req.getHeader("Accept"));
    }

    protected final Twdb getDb() {
        return db;
    }
}