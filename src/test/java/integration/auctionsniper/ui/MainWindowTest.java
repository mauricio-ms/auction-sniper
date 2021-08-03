package integration.auctionsniper.ui;

import auctionsniper.SniperPortfolio;
import auctionsniper.UserRequestListener.Item;
import auctionsniper.ui.MainWindow;
import com.objogate.wl.swing.probe.ValueMatcherProbe;
import endtoend.auctionsniper.AuctionSniperDriver;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

public class MainWindowTest {

    private final SniperPortfolio portfolio = new SniperPortfolio();
    private final MainWindow mainWindow = new MainWindow(portfolio);
    private final AuctionSniperDriver driver = new AuctionSniperDriver(100);

    @Test
    public void makesUserRequestWhenJoinButtonClicked() {
        ValueMatcherProbe<Item> itemProbe = new ValueMatcherProbe<>(
                equalTo(new Item("itemId", 789)), "item request");

        mainWindow.addUserRequestListener(itemProbe::setReceivedValue);

        driver.startBiddingFor("itemId", 789);
        driver.check(itemProbe);
    }
}