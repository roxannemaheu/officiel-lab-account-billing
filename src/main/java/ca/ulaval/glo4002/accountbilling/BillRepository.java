package ca.ulaval.glo4002.accountbilling;

import java.util.List;

public interface BillRepository {
    Bill findBillById(BillId id) throws BillNotFoundException;

    List<Bill> findAllByClient(ClientId clientId);

    void save(Bill bill);
}