package cornerstone.webapp.services.accounts.management.enums;

public enum SYSTEM_ROLE_ENUM {
    NONE(0),
    USER(1),
    ADMIN(2);

    private final int id;
    SYSTEM_ROLE_ENUM(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
