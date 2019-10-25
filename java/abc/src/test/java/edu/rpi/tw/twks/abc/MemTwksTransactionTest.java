package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.test.TwksTransactionTest;

public final class MemTwksTransactionTest extends TwksTransactionTest {
    @Override
    protected Twks newTwks(final TwksConfiguration configuration) {
        return new MemTwks(configuration);
    }
}
