package ca.ulaval.glo4002.accountbilling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class AccountBillingServiceTest {
	private static final BillId BILL_ID = new BillId(12L);
	private static final ClientId CLIENT_ID = new ClientId(18L);

	private AccountBillingService service;
	private Bill bill;
	private List<Bill> otherBillsForClient;
	private List<Bill> persistedBills;

	@Before
	public void setUp() {
		otherBillsForClient = new ArrayList<>();
		persistedBills = new ArrayList<>();
		service = new TestableAccountBillingService();
	}

	@Test(expected = BillNotFoundException.class)
	public void givenAnIdForABillThatDoesntExist_whenCancelling_thenErrorsOut() {
		service.cancelInvoiceAndRedistributeFunds(BILL_ID);
	}

	@Test
	public void givenBillIsNotCancel_whenCancelling_thenCancelsBill() {
		bill = new Bill(CLIENT_ID, 10);

		service.cancelInvoiceAndRedistributeFunds(BILL_ID);

		assertTrue(bill.isCancelled());
	}

	@Test
	public void givenBillIsAlreadyCancelled_whenCancelling_thenDoesNotCancelBill() {
		bill = new Bill(CLIENT_ID, 10);
		bill.cancel();

		service.cancelInvoiceAndRedistributeFunds(BILL_ID);
	}

	@Test
	public void whenCancelling_thenPersistsCancelledBill() {
		bill = new Bill(CLIENT_ID, 10);

		service.cancelInvoiceAndRedistributeFunds(BILL_ID);

		assertBillPersisted(bill);
	}

	@Test
	public void givenBillHasOneAllocationAndClientHasOneOtherUnpaidBill_whenCancelling_thenAllocationIsDistributedToOtherBill() {
		bill = new Bill(CLIENT_ID, 10);
		bill.addAllocation(new Allocation(10));
		Bill unpaidBill = new Bill(CLIENT_ID, 20);
		givenOtherBillsForClient(unpaidBill);

		service.cancelInvoiceAndRedistributeFunds(BILL_ID);

		assertEquals(10, unpaidBill.getRemainingAmount());
	}

	@Test
	public void givenBillHasCancelledBillInClientBIlls_whenCancelling_thenCurrentBillIsNotConsidered() {
		bill = new Bill(CLIENT_ID, 20);
		bill.addAllocation(new Allocation(10));
		Bill unpaidBill = new Bill(CLIENT_ID, 20);
		givenOtherBillsForClient(bill, unpaidBill);

		service.cancelInvoiceAndRedistributeFunds(BILL_ID);

		assertEquals(10, bill.getRemainingAmount());
	}

	@Test
	public void givenBillHasOneAllocationAndClientHasOtherSmallUnpaidBills_whenCancelling_thenAllocationIsDistributedToTheTwoOtherBills() {
		bill = new Bill(CLIENT_ID, 10);
		bill.addAllocation(new Allocation(10));
		Bill smallUnpaidBill = new Bill(CLIENT_ID, 2);
		Bill unpaidBill = new Bill(CLIENT_ID, 20);
		givenOtherBillsForClient(smallUnpaidBill, unpaidBill);

		service.cancelInvoiceAndRedistributeFunds(BILL_ID);

		assertEquals(0, smallUnpaidBill.getRemainingAmount());
		assertEquals(12, unpaidBill.getRemainingAmount());
	}

	@Test
	public void givenBillHasMultipleAllocations_whenCancelling_thenAllAllocationsAreRedistributed() {
		bill = new Bill(CLIENT_ID, 10);
		bill.addAllocation(new Allocation(6));
		bill.addAllocation(new Allocation(4));
		Bill unpaidBill = new Bill(CLIENT_ID, 20);
		givenOtherBillsForClient(unpaidBill);

		service.cancelInvoiceAndRedistributeFunds(BILL_ID);

		assertEquals(10, unpaidBill.getRemainingAmount());
	}

	@Test
	public void givenBillHasAllocationsToRedistribute_whenCancelling_thenBillArePersisted() {
		bill = new Bill(CLIENT_ID, 10);
		bill.addAllocation(new Allocation(10));
		Bill unpaidBill = new Bill(CLIENT_ID, 20);
		givenOtherBillsForClient(unpaidBill);

		service.cancelInvoiceAndRedistributeFunds(BILL_ID);

		assertBillPersisted(unpaidBill);
	}

	private void givenOtherBillsForClient(Bill... otherBills) {
		otherBillsForClient.addAll(Arrays.asList(otherBills));
	}

	private void assertBillPersisted(Bill bill) {
		boolean persisted = persistedBills.contains(bill);
		assertTrue("Wanted bill to be persisted " + bill, persisted);
	}

	class TestableAccountBillingService extends AccountBillingService {
		@Override
		protected Bill getBillById(BillId id) {
			return bill;
		}

		@Override
		protected void persistBill(Bill bill) {
			persistedBills.add(bill);
		}

		@Override
		protected List<Bill> getBillsForClient(ClientId clientId) {
			return otherBillsForClient;
		}
	}
}
