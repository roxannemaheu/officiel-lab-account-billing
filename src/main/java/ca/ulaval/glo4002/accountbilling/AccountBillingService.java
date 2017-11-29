package ca.ulaval.glo4002.accountbilling;

import java.util.List;

public class AccountBillingService {

	public void cancelInvoiceAndRedistributeFunds(BillId id) {
		Bill billToCancel = getBillById(id);
		if (billToCancel != null) {
			ClientId cid = billToCancel.getClientId();

			if (!billToCancel.isCancelled()) {
				billToCancel.cancel();
			}
			persistBill(billToCancel);

			List<Allocation> allocationsToRedistribute = billToCancel.getAllocations();

			for (Allocation allocationToRedistribute : allocationsToRedistribute) {
				List<Bill> bills = getBillsForClient(cid);
				int amountToRedistribute = allocationToRedistribute.getAmount();

				for (Bill billCandidate : bills) {
					if (billToCancel != billCandidate) {
						int remainingAmount = billCandidate.getRemainingAmount();
						Allocation newAllocation;
						if (remainingAmount <= amountToRedistribute) {
							newAllocation = new Allocation(remainingAmount);
							amountToRedistribute -= remainingAmount;
						} else {
							newAllocation = new Allocation(amountToRedistribute);
							amountToRedistribute = 0;
						}

						billCandidate.addAllocation(newAllocation);
						persistBill(billCandidate);
					}

					if (amountToRedistribute == 0) {
						break;
					}
				}
			}
		} else {
			throw new BillNotFoundException();
		}
	}

	protected List<Bill> getBillsForClient(ClientId clientId) {
		return BillDAO.getInstance().findAllByClient(clientId);
	}

	protected void persistBill(Bill bill) {
		BillDAO.getInstance().persist(bill);
	}

	protected Bill getBillById(BillId id) {
		return BillDAO.getInstance().findBill(id);
	}
}
