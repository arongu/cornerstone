package cornerstone.webapp.services.accounts.management.enums;

public enum MULTI_ACCOUNT_ROLE_ENUM {
    NOT_APPLICABLE(0),
    MULTI_ACCOUNT_USER(1),
    MULTI_ACCOUNT_ADMIN(2),
    MULTI_ACCOUNT_OWNER(3);

    private final int id;
    MULTI_ACCOUNT_ROLE_ENUM(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
