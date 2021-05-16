package test.java;

public class AuctionSniperEndToEndTest {

    private final FakeAuctionServer auction = new FakeAuctionServer("item-54321");
    private final ApplicationRunner application = new ApplicationRunner();

    @Test public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        auction.startSellingItem();                     // Step 1
        application.startBiddingIn(auction);            // Step 2
        auction.hasReceivedJoinRequestFromSniper();     // Step 3
        auction.announceClosed();                       // Step 4
        application.showSniperHasLostAuction();         // Step 5
    }

    @After public void stopAuction() {
        auction.stop();
    }

    @After public void stopApplication() {
        application.stop();
    }
}
