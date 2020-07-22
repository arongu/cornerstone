package cornerstone.webapp.services.account.admin;

import java.sql.Timestamp;

public class AccountResultSet {
    public final int account_id;
    public final Timestamp account_registration_ts;
    public final boolean account_locked;
    public final Timestamp account_locked_ts;
    public final String account_lock_reason;
    public final int account_login_attempts;
    public final String email_address;
    public final Timestamp email_address_ts;
    public final boolean email_address_verified;
    public final Timestamp email_address_verified_ts;
    public final String password_hash;
    public final Timestamp password_hash_ts;

    public AccountResultSet(final int account_id,
                            final Timestamp account_registration_ts,
                            final boolean account_locked,
                            final Timestamp account_locked_ts,
                            final String account_lock_reason,
                            final int account_login_attempts,
                            final String email_address,
                            final Timestamp email_address_ts,
                            final boolean email_address_verified,
                            final Timestamp email_address_verified_ts,
                            final String password_hash,
                            final Timestamp password_hash_ts) {

        this.account_id = account_id;
        this.account_registration_ts = account_registration_ts;

        this.account_locked = account_locked;
        this.account_locked_ts = account_locked_ts;
        this.account_lock_reason = account_lock_reason;

        this.account_login_attempts = account_login_attempts;
        this.email_address = email_address;
        this.email_address_ts = email_address_ts;

        this.email_address_verified = email_address_verified;
        this.email_address_verified_ts = email_address_verified_ts;

        this.password_hash = password_hash;
        this.password_hash_ts = password_hash_ts;
    }
}
