package auctionsniper.xmpp;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.logging.LogManager;
import java.util.logging.Logger;

@RunWith(JMock.class)
public class LoggingXMPPFailureReporterTest {

    private final Mockery context = new Mockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }};
    final Logger logger = context.mock(Logger.class);
    final LoggingXMPPFailureReporter reporter = new LoggingXMPPFailureReporter(logger);

    @Test
    public void writesMessageTranslationFailureToLog() {
        context.checking(new Expectations() {{
            oneOf(logger).severe("<auction id> " +
                    "Could not translate message \"bad message\" " +
                    "because \"java.lang.Exception: bad\"");
        }});

        reporter.cannotTranslateMessage("auction id", "bad message", new Exception("bad"));
    }

    @AfterClass
    public static void resetLogging() {
        LogManager.getLogManager().reset();
    }
}