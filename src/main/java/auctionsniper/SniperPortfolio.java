package auctionsniper;

import org.jmock.example.announcer.Announcer;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class SniperPortfolio implements SniperCollector {

    public interface PortfolioListener extends EventListener {
        void sniperAdded(AuctionSniper sniper);
    }

    private final Announcer<PortfolioListener> announcer = Announcer.to(PortfolioListener.class);
    private final List<AuctionSniper> snipers = new ArrayList<>();

    @Override
    public void addSniper(AuctionSniper sniper) {
        snipers.add(sniper);
        announcer.announce().sniperAdded(sniper);
    }

    public void addPortfolioListener(PortfolioListener listener) {
        announcer.addListener(listener);
    }
}
