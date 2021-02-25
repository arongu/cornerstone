package cornerstone.webapp.services.accounts.management;

public enum UserRole {
    NO_ROLE(0),
    USER(1),
    SUPER(5),
    ADMIN(7);

    private final int id;
    UserRole(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
