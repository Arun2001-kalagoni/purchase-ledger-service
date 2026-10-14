package com.arunkalagoni.ledger.adapter.cassandra;

import com.arunkalagoni.ledger.domain.PurchaseEvent;
import com.arunkalagoni.ledger.domain.port.EventHistoryStore;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class CassandraEventHistoryStore implements EventHistoryStore {

    private final CassandraTemplate cassandra;

    public CassandraEventHistoryStore(CassandraTemplate cassandra) {
        this.cassandra = cassandra;
    }

    @Override
    public void append(PurchaseEvent event) {
        cassandra.insert(PurchaseEventRow.from(event));
    }

    @Override
    public List<PurchaseEvent> findByPurchaseDate(LocalDate date) {
        return cassandra.select(
                        Query.query(Criteria.where("purchase_date").is(date)), PurchaseEventRow.class)
                .stream()
                .map(PurchaseEventRow::toDomain)
                .toList();
    }
}
