package cornerstone.webapp.services.accounts.management.enums;

public enum ACCOUNT_TYPE_ENUM {
    SUB_ACCOUNT(0),
    SINGLE_ACCOUNT(1),
    MULTI_ACCOUNT(2);

    private final int id;
    ACCOUNT_TYPE_ENUM(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
