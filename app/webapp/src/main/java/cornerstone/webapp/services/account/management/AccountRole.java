package cornerstone.webapp.services.account.management;

public enum AccountRole {
    NO_ROLE(0),
    USER(1),
    SUPER(5),
    ADMIN(7);

    private int id;
    AccountRole(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(final short id) {
        this.id = id;
    }
}
