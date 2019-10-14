package edu.rpi.tw.twks.test;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.test.ApisTest;
import org.apache.jena.query.ReadWrite;

public abstract class TwksTransactionTest extends ApisTest<TwksTransaction> {
    @Override
    protected final TwksTransaction openSystemUnderTest() throws Exception {
        return newTwks().beginTransaction(ReadWrite.WRITE);
    }

    @Override
    protected final void closeSystemUnderTest(final TwksTransaction sut) {
        sut.commit();
        sut.close();
    }

    protected abstract Twks newTwks();
}