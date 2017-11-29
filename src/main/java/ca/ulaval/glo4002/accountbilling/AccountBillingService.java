package ca.ulaval.glo4002.accountbilling;

import java.util.List;

public class AccountBillingService {

	public void cancelInvoiceAndRedistributeFunds(BillId id) {
		Bill billToCancel = BillDAO.getInstance().findBill(id);
		if (billToCancel != null) {
			ClientId cid = billToCancel.getClientId();

			if (!billToCancel.isCancelled()) {
				billToCancel.cancel();
			}
			BillDAO.getInstance().persist(billToCancel);

			List<Allocation> allocationsToRedistribute = billToCancel.getAllocations();

			for (Allocation allocationToRedistribute : allocationsToRedistribute) {
				List<Bill> bills = BillDAO.getInstance().findAllByClient(cid);
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
						BillDAO.getInstance().persist(billCandidate);
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
}
