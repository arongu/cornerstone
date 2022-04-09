package cornerstone.webapp.services.accounts.management;

import java.sql.Timestamp;

public class AccountResultSet {
    public final String    account_group_id;
    public final String    account_id;
    public final Timestamp account_creation_ts;
    public final boolean   account_locked;
    public final Timestamp account_locked_ts;
    public final String    account_lock_reason;
    public final Timestamp account_lock_reason_ts;
    public final int       login_attempts;
    public final String    last_login_attempt_ip;
    public final Timestamp last_login_attempt_ip_ts;
    public final String    last_successful_login_ip;
    public final Timestamp last_successful_login_ip_ts;
    public final String    email_address;
    public final Timestamp email_address_ts;
    public final boolean   email_address_verified;
    public final Timestamp email_address_verified_ts;
    public final String    password_hash;
    public final Timestamp password_hash_ts;
    public final String    superpowers;
    public final Timestamp superpowers_ts;

    public AccountResultSet(final String    account_group_id,
                            final String    account_id,
                            final Timestamp account_creation_ts,
                            final boolean   account_locked,
                            final Timestamp account_locked_ts,
                            final String    account_lock_reason,
                            final Timestamp account_lock_reason_ts,
                            final int       login_attempts,
                            final String    last_login_attempt_ip,
                            final Timestamp last_login_attempt_ip_ts,
                            final String    last_successful_login_ip,
                            final Timestamp last_successful_login_ip_ts,
                            final String    email_address,
                            final Timestamp email_address_ts,
                            final boolean   email_address_verified,
                            final Timestamp email_address_verified_ts,
                            final String password_hash,
                            final Timestamp password_hash_ts,
                            final String    superpowers,
                            final Timestamp superpowers_ts) {

        this.account_group_id            = account_group_id;
        this.account_id                  = account_id;
        this.account_creation_ts         = account_creation_ts;
        this.account_locked              = account_locked;
        this.account_locked_ts           = account_locked_ts;
        this.account_lock_reason         = account_lock_reason;
        this.account_lock_reason_ts      = account_lock_reason_ts;
        this.login_attempts              = login_attempts;
        this.last_login_attempt_ip       = last_login_attempt_ip;
        this.last_login_attempt_ip_ts    = last_login_attempt_ip_ts;
        this.last_successful_login_ip    = last_successful_login_ip;
        this.last_successful_login_ip_ts = last_successful_login_ip_ts;
        this.email_address               = email_address;
        this.email_address_ts            = email_address_ts;
        this.email_address_verified      = email_address_verified;
        this.email_address_verified_ts   = email_address_verified_ts;
        this.password_hash               = password_hash;
        this.password_hash_ts            = password_hash_ts;
        this.superpowers                 = superpowers;
        this.superpowers_ts              = superpowers_ts;
    }
}
