package auctionsniper;

public interface Auction {

    void addAuctionEventListener(AuctionEventListener auctionEventListener);

    void bid(int amount);

    void join();
}
