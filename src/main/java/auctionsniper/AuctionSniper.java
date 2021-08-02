package auctionsniper;

import org.jmock.example.announcer.Announcer;

public class AuctionSniper implements AuctionEventListener {

    private final Announcer<SniperListener> listeners = Announcer.to(SniperListener.class);

    private final Auction auction;
    private SniperSnapshot snapshot;

    public AuctionSniper(String itemId, Auction auction) {
        this.auction = auction;
        snapshot = SniperSnapshot.joining(itemId);
    }

    public void addSniperListener(SniperListener sniperListener) {
        listeners.addListener(sniperListener);
    }

    @Override
    public void auctionClosed() {
        snapshot = snapshot.closed();
        notifyChange();
    }

    @Override
    public void currentPrice(int price, int increment, PriceSource priceSource) {
        switch (priceSource) {
            case FromSniper -> snapshot = snapshot.winning(price);
            case FromOtherBidder -> {
                int bid = price + increment;
                auction.bid(bid);
                snapshot = snapshot.bidding(price, bid);
            }
        }
        notifyChange();
    }

    private void notifyChange() {
        listeners.announce().sniperStateChanged(snapshot);
    }

    public SniperSnapshot getSnapshot() {
        return snapshot;
    }
}
