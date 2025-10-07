package ca.ulaval.glo4002.accountbilling;

public class BillId {
    public long id;

    public BillId(long id) {
        if (id == 0) {
            throw new InvalidIdException();
        }
        this.id = id;
    }


}
