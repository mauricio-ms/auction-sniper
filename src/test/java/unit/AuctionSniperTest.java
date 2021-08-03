package unit;

import auctionsniper.*;
import auctionsniper.UserRequestListener.Item;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static auctionsniper.AuctionEventListener.PriceSource.FromOtherBidder;
import static auctionsniper.AuctionEventListener.PriceSource.FromSniper;
import static auctionsniper.SniperState.*;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JMock.class)
public class AuctionSniperTest {

    protected static final String ITEM_ID = "itemId";
    public static final Item ITEM = new Item(ITEM_ID, 1234);

    private final Mockery context = new Mockery();
    private final States sniperState = context.states("sniper");

    private final Auction auction = context.mock(Auction.class);
    private final SniperListener sniperListener = context.mock(SniperListener.class);
    private final AuctionSniper sniper = new AuctionSniper(ITEM, auction);

    @Before
    public void attachListener() {
        sniper.addSniperListener(sniperListener);
    }

    @Test
    public void reportsLostWhenAuctionClosesImmediately() {
        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(with(aSniperThatIs(LOST)));
        }});

        sniper.auctionClosed();
    }

    @Test
    public void reportsLostIfAuctionClosesWhenBidding() {
        ignoringAuction();
        allowSniperBidding();
        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(with(aSniperThatIs(LOST)));
            when(sniperState.is("bidding"));
        }});

        sniper.currentPrice(123, 45, FromOtherBidder);
        sniper.auctionClosed();
    }

    @Test
    public void reportsWonIfAuctionClosesWhenWinning() {
        ignoringAuction();
        allowSniperWinning();
        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(with(aSniperThatIs(WON)));
            when(sniperState.is("winning"));
        }});

        sniper.currentPrice(123, 45, FromSniper);
        sniper.auctionClosed();
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;

        context.checking(new Expectations() {{
            oneOf(auction).bid(price + increment);
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, BIDDING));
        }});

        sniper.currentPrice(price, increment, FromOtherBidder);
    }

    @Test
    public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        ignoringAuction();
        allowSniperBidding();
        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(
                    new SniperSnapshot(ITEM_ID, 135, 135, WINNING)
            );
            when(sniperState.is("bidding"));
        }});

        sniper.currentPrice(123, 12, FromOtherBidder);
        sniper.currentPrice(135, 45, FromSniper);
    }

    @Test
    public void doesNotBidAndReportsLosingIfSubsequentPriceIsAboveStopPrice() {
        allowSniperBidding();
        context.checking(new Expectations() {{
            int bid = 123 + 45;
            allowing(auction).bid(bid);
            atLeast(1).of(sniperListener).sniperStateChanged(
                    new SniperSnapshot(ITEM_ID, 2345, bid, LOSING));
            when(sniperState.is("bidding"));
        }});

        sniper.currentPrice(123, 45, FromOtherBidder);
        sniper.currentPrice(2345, 25, FromOtherBidder);
    }

    @Test
    public void doesNotBidAndReportsLosingIfFirstPriceIsAboveStopPrice() {
        int price = 1233;
        int increment = 25;

        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(
                    new SniperSnapshot(ITEM_ID, price, 0, LOSING));
        }});

        sniper.currentPrice(price, increment, FromOtherBidder);
    }

    @Test
    public void reportsLostIfAuctionClosesWhenLosing() {
        allowSniperLosing();
        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(
                    new SniperSnapshot(ITEM_ID, 1230, 0, LOST));
            when(sniperState.is("losing"));
        }});

        sniper.currentPrice(1230, 456, FromOtherBidder);
        sniper.auctionClosed();
    }

    @Test
    public void continuesToBeLosingOnceStopPriceHasBeenReached() {
        Sequence states = context.sequence("sniper states");
        int price1 = 1233;
        int price2 = 1258;

        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(
                    new SniperSnapshot(ITEM_ID, price1, 0, LOSING)); inSequence(states);
            atLeast(1).of(sniperListener).sniperStateChanged(
                    new SniperSnapshot(ITEM_ID, price2, 0, LOSING)); inSequence(states);
        }});

        sniper.currentPrice(price1, 25, FromOtherBidder);
        sniper.currentPrice(price2, 25, FromOtherBidder);
    }

    @Test
    public void doesNotBidAndReportsLosingIfPriceAfterWinningIsAboveStopPrice() {
        int price = 1233;
        int increment = 25;

        allowSniperBidding();
        allowSniperWinning();
        context.checking(new Expectations() {{
            int bid = 123 + 45;
            allowing(auction).bid(bid);
            atLeast(1).of(sniperListener).sniperStateChanged(
                    new SniperSnapshot(ITEM_ID, price, bid, LOSING));
            when(sniperState.is("winning"));
        }});

        sniper.currentPrice(123, 45, FromOtherBidder);
        sniper.currentPrice(168, 45, FromSniper);
        sniper.currentPrice(price, increment, FromOtherBidder);
    }

    private void ignoringAuction() {
        context.checking(new Expectations() {{
            ignoring(auction);
        }});
    }

    private void allowSniperBidding() {
        allowSniperStateChange(BIDDING, "bidding");
    }

    private void allowSniperWinning() {
        allowSniperStateChange(WINNING, "winning");
    }

    private void allowSniperLosing() {
        allowSniperStateChange(LOSING, "losing");
    }

    private void allowSniperStateChange(SniperState newState, String oldState) {
        context.checking(new Expectations() {{
            allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(newState)));
            then(sniperState.is(oldState));
        }});
    }

    private Matcher<SniperSnapshot> aSniperThatIs(final SniperState state) {
        return new FeatureMatcher<>(equalTo(state), "sniper that is", "was") {
            @Override
            protected SniperState featureValueOf(SniperSnapshot actual) {
                return actual.state;
            }
        };
    }
}
