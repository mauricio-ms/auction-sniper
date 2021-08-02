package integration.auctionsniper.ui;

import auctionsniper.ui.MainWindow;
import endtoend.auctionsniper.AuctionSniperDriver;
import auctionsniper.SniperPortfolio;
import com.objogate.wl.swing.probe.ValueMatcherProbe;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

public class MainWindowTest {

    private final SniperPortfolio portfolio = new SniperPortfolio();
    private final MainWindow mainWindow = new MainWindow(portfolio);
    private final AuctionSniperDriver driver = new AuctionSniperDriver(100);

    @Test
    public void makesUserRequestWhenJoinButtonClicked() {
        ValueMatcherProbe<String> buttonProbe = new ValueMatcherProbe<>(
                equalTo("itemId"), "join request");

        mainWindow.addUserRequestListener(buttonProbe::setReceivedValue);

        driver.startBiddingFor("itemId");
        driver.check(buttonProbe);
    }
}