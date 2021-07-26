package auctionsniper.ui;

import auctionsniper.SniperSnapshot;
import com.objogate.exception.Defect;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;

@RunWith(JMock.class)
public class SnipersTableModelTest {

    private final Mockery context = new Mockery();
    private final TableModelListener listener = context.mock(TableModelListener.class);
    private final SnipersTableModel model = new SnipersTableModel();

    @Before
    public void attachModelListener() {
        model.addTableModelListener(listener);
    }

    @Test
    public void hasEnoughColumns() {
        assertThat(model.getColumnCount(), equalTo(Column.values().length));
    }

    @Test
    public void setSniperValuesInColumns() {
        SniperSnapshot joining = SniperSnapshot.joining("item id");
        SniperSnapshot bidding = joining.bidding(555, 667);
        context.checking(new Expectations() {{
            allowing(listener).tableChanged(with(anyInsertionEvent()));

            oneOf(listener).tableChanged(with(aChangeInRow(0)));
        }});

        model.addSniper(joining);
        model.sniperStateChanged(bidding);

        assertRowMatchesSnapshot(0, bidding);
    }

    @Test
    public void setsUpColumnHeadings() {
        for (Column column : Column.values()) {
            assertEquals(column.name, model.getColumnName(column.ordinal()));
        }
    }

    @Test
    public void notifiesListenersWhenAddingASniper() {
        SniperSnapshot joining = SniperSnapshot.joining("item123");
        context.checking(new Expectations() {{
            oneOf(listener).tableChanged(with(anInsertionAtRow(0)));
        }});

        assertEquals(0, model.getRowCount());

        model.addSniper(joining);

        assertEquals(1, model.getRowCount());
        assertRowMatchesSnapshot(0, joining);
    }

    @Test
    public void holdsSnipersInAdditionOrder() {
        context.checking(new Expectations() {{
            ignoring(listener);
        }});

        model.addSniper(SniperSnapshot.joining("item 0"));
        model.addSniper(SniperSnapshot.joining("item 1"));

        assertEquals("item 0", cellValue(0, Column.ITEM_IDENTIFIER));
        assertEquals("item 1", cellValue(1, Column.ITEM_IDENTIFIER));
    }

    @Test
    public void updatesCorrectRowForSniper() {
        context.checking(new Expectations() {{
            ignoring(listener);
        }});

        model.addSniper(SniperSnapshot.joining("item 0"));
        SniperSnapshot itemOne = SniperSnapshot.joining("item 1");
        model.addSniper(itemOne);

        model.sniperStateChanged(itemOne.bidding(100, 120));

        assertEquals(0, cellValue(0, Column.LAST_PRICE));
        assertEquals(0, cellValue(0, Column.LAST_BID));
        assertEquals(100, cellValue(1, Column.LAST_PRICE));
        assertEquals(120, cellValue(1, Column.LAST_BID));
    }

    @Test(expected = Defect.class)
    public void throwsDefectIfNoExistingSniperForAnUpdate() {
        model.sniperStateChanged(SniperSnapshot.joining("item 0"));
    }

    private void assertRowMatchesSnapshot(int row, SniperSnapshot snapshot) {
        assertEquals(snapshot.itemId, cellValue(row, Column.ITEM_IDENTIFIER));
        assertEquals(snapshot.lastPrice, cellValue(row, Column.LAST_PRICE));
        assertEquals(snapshot.lastBid, cellValue(row, Column.LAST_BID));
        assertEquals(SnipersTableModel.textFor(snapshot.state), cellValue(row, Column.SNIPER_STATE));
    }

    private Object cellValue(int rowIndex, Column column) {
        return model.getValueAt(rowIndex, column.ordinal());
    }

    private Matcher<TableModelEvent> anyInsertionEvent() {
        return hasProperty("type", equalTo(TableModelEvent.INSERT));
    }

    private Matcher<TableModelEvent> anInsertionAtRow(int row) {
        return samePropertyValuesAs(new TableModelEvent(model, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    private Matcher<TableModelEvent> aChangeInRow(int row) {
        return samePropertyValuesAs(new TableModelEvent(model, row));
    }
}