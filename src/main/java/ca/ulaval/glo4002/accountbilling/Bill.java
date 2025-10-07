package ca.ulaval.glo4002.accountbilling;

import java.util.ArrayList;
import java.util.List;

public class Bill {

    private BillId id;
    private ClientId clientId;
    private List<Allocation> allocations = new ArrayList<>();
    private boolean cancelled = false;
    private int total;

    public Bill(BillId id, ClientId clientId, int total) {
        this.id = id;
        this.clientId = clientId;
        this.total = total;
    }

    int createAllocation(int amount) {
        int remainingAmount = getRemainingAmount();
        Allocation allocation;
        if (remainingAmount <= amount) {
            allocation = new Allocation(remainingAmount);
            amount -= remainingAmount;
        } else {
            allocation = new Allocation(amount);
            amount = 0;
        }
        addAllocation(allocation);
        return amount;
    }

    public ClientId getClientId() {
        return clientId;
    }

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void addAllocation(Allocation allocation) {
        allocations.add(allocation);
    }

    public List<Allocation> getAllocations() {
        return allocations;
    }

    public int getRemainingAmount() {
        return total - allocations.stream().mapToInt(Allocation::getAmount).sum();
    }

}
