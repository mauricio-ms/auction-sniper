package auctionsniper.xmpp;

import auctionsniper.AuctionEventListener;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import static auctionsniper.ApplicationRunner.SNIPER_ID;
import static auctionsniper.AuctionEventListener.PriceSource;

@RunWith(JMock.class)
public class AuctionMessageTranslatorTest {

    public static final Chat UNUSED_CHAT = null;

    private final Mockery context = new Mockery();
    private final AuctionEventListener listener = context.mock(AuctionEventListener.class);
    private final XMPPFailureReporter failureReporter = context.mock(XMPPFailureReporter.class);

    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(SNIPER_ID, listener, failureReporter);

    @Test
    public void notifiesAuctionClosedWhenCloseMessageReceived() {
        context.checking(new Expectations() {{
            oneOf(listener).auctionClosed();
        }});

        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: CLOSE;");

        translator.processMessage(UNUSED_CHAT, message);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder() {
        context.checking(new Expectations() {{
            exactly(1).of(listener).currentPrice(192, 7, PriceSource.FromOtherBidder);
        }});

        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;");

        translator.processMessage(UNUSED_CHAT, message);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() {
        context.checking(new Expectations() {{
            exactly(1).of(listener).currentPrice(234, 5, PriceSource.FromSniper);
        }});

        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";");

        translator.processMessage(UNUSED_CHAT, message);
    }

    @Test
    public void notifiesAuctionFailedWhenBadMessageReceived() {
        String badMessage = "a bad message";
        expectFailureWithMessage(badMessage);

        translator.processMessage(UNUSED_CHAT, message(badMessage));
    }

    @Test
    public void notifiesAuctionFailedWhenEventTypeMissing() {
        String badMessage = "SOLVersion: 1.1; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";";
        expectFailureWithMessage(badMessage);

        translator.processMessage(UNUSED_CHAT, message(badMessage));
    }

    @Test
    public void notifiesAuctionFailedWhenCurrentPriceMissing() {
        String badMessage = "SOLVersion: 1.1; Event: PRICE; Increment: 5; Bidder: " + SNIPER_ID + ";";
        expectFailureWithMessage(badMessage);

        translator.processMessage(UNUSED_CHAT, message(badMessage));
    }

    @Test
    public void notifiesAuctionFailedWhenIncrementMissing() {
        String badMessage = "SOLVersion: 1.1; Event: PRICE; CurrentPrice: 234; Bidder: " + SNIPER_ID + ";";
        expectFailureWithMessage(badMessage);

        translator.processMessage(UNUSED_CHAT, message(badMessage));
    }

    @Test
    public void notifiesAuctionFailedWhenCurrentBidderMissing() {
        String badMessage = "SOLVersion: 1.1; Event: PRICE; CurrentPrice: 234; Increment: 5;";
        expectFailureWithMessage(badMessage);

        translator.processMessage(UNUSED_CHAT, message(badMessage));
    }

    private Message message(String badMessage) {
        Message message = new Message();
        message.setBody(badMessage);
        return message;
    }

    private void expectFailureWithMessage(String badMessage) {
        context.checking(new Expectations() {{
            oneOf(listener).auctionFailed();
            oneOf(failureReporter).cannotTranslateMessage(
                    with(SNIPER_ID), with(badMessage), with(any(Exception.class)));
        }});
    }
}
