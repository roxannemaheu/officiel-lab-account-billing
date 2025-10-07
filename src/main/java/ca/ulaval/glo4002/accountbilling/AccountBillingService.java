package ca.ulaval.glo4002.accountbilling;

import java.util.List;
import java.util.Objects;

public class AccountBillingService {

    private final BillRepository billRepository;

    public AccountBillingService(BillRepository billRepository) {
        this.billRepository = Objects.requireNonNull(billRepository, "billRepository must not be null");
    }

    public void manageBill(BillId id) {
        Bill currentBill = billRepository.findBillById(id);
        cancelInvoice(currentBill);
        redistributeFunds(currentBill);
    }

    public void cancelInvoice(Bill currentBill) {
        if (!currentBill.isCancelled()) {
            currentBill.cancel();
            billRepository.save(currentBill);
        }
    }

    public void redistributeFunds(Bill cancelledBill) {
        List<Allocation> currentBillAllocations = cancelledBill.getAllocations();
        ClientId clientId = cancelledBill.getClientId();

        for (Allocation allocation : currentBillAllocations) {
            List<Bill> bills = billRepository.findAllByClient(clientId);
            int amount = allocation.getAmount();

            for (Bill bill : bills) {
                if (cancelledBill.equals(bill)) {
                    cancelledBill.createAllocation(amount);
                    billRepository.save(bill);
                }
                if (amount == 0) {
                    break;
                }
            }
        }
    }
}
