package edu.rpi.tw.twks.tdb;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.test.TwksTransactionTest;

public final class Tdb2TwksTransactionTest extends TwksTransactionTest {
    @Override
    protected Twks newTwks(final TwksConfiguration configuration) {
        return new Tdb2Twks(configuration);
    }
}
