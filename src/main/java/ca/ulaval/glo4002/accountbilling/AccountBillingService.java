package ca.ulaval.glo4002.accountbilling;

import java.util.List;

public class AccountBillingService {

    public BillDAO billdao = BillDAO.getInstance();

    public void manageBill(BillId id) {
        Bill currentBill = billdao.findBill(id);
        if (currentBill != null) {
            cancelInvoice(currentBill);
            redistributeFunds(currentBill);
        } else {
            throw new BillNotFoundException();
        }
    }

    public void cancelInvoice(Bill currentBill) {
        if (!currentBill.isCancelled()) {
            currentBill.cancel();
            billdao.persist(currentBill);
        }
    }

    public void redistributeFunds(Bill currentBill) {
        List<Allocation> currentBillAllocations = currentBill.getAllocations();
        ClientId cid = currentBill.getClientId();

        for (Allocation allocation : currentBillAllocations) {
            List<Bill> bills = billdao.findAllByClient(cid);
            int amount = allocation.getAmount();

            for (Bill bill : bills) {
                if (currentBill != bill) {
                    currentBill.createAllocation(amount);
                    billdao.persist(bill);
                }
                if (amount == 0) {
                    break;
                }
            }
        }
    }
}
