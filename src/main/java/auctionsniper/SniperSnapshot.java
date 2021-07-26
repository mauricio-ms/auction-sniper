package auctionsniper;

import java.util.Objects;

public class SniperSnapshot {

    public final String itemId;
    public final int lastPrice;
    public final int lastBid;
    public final SniperState state;

    public static SniperSnapshot joining(String itemId) {
        return new SniperSnapshot(itemId, 0, 0, SniperState.JOINING);
    }

    public SniperSnapshot bidding(int newLastPrice, int newLastBid) {
        return new SniperSnapshot(itemId, newLastPrice, newLastBid, SniperState.BIDDING);
    }

    public SniperSnapshot winning(int newLastPrice) {
        return new SniperSnapshot(itemId, newLastPrice, lastBid, SniperState.WINNING);
    }

    public SniperSnapshot closed() {
        return new SniperSnapshot(itemId, lastPrice, lastBid, state.whenAuctionClosed());
    }

    public SniperSnapshot(String itemId, int lastPrice, int lastBid, SniperState state) {
        this.itemId = itemId;
        this.lastPrice = lastPrice;
        this.lastBid = lastBid;
        this.state = state;
    }

    public boolean isForSameItemAs(SniperSnapshot sniperSnapshot) {
        return itemId.equals(sniperSnapshot.itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SniperSnapshot that = (SniperSnapshot) o;
        return lastPrice == that.lastPrice && lastBid == that.lastBid && Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, lastPrice, lastBid);
    }
}
