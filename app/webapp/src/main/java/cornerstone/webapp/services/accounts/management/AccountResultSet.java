package cornerstone.webapp.services.accounts.management;

import java.sql.Timestamp;
import java.util.UUID;

public class AccountResultSet {
    public final String    role_name;
    public final int       role_id;
    public final String    account_id;
    public final Timestamp account_registration_ts;
    public final boolean   account_locked;
    public final Timestamp account_locked_ts;
    public final String    account_lock_reason;
    public final int       account_login_attempts;
    public final String    email_address;
    public final Timestamp email_address_ts;
    public final boolean   email_address_verified;
    public final Timestamp email_address_verified_ts;
    public final String    password_hash;
    public final Timestamp password_hash_ts;
    public final boolean   delete;
    public final boolean   get;
    public final boolean   head;
    public final boolean   options;
    public final boolean   patch;
    public final boolean   post;
    public final boolean   put;

    public AccountResultSet(final String    system_role_name,
                            final int       system_role_id,
                            final String    account_id,
                            final Timestamp account_registration_ts,
                            final int       account_type_id,
                            final String    account_type_name,
                            final boolean   account_locked,
                            final Timestamp account_locked_ts,
                            final String    account_lock_reason,
                            final int       account_login_attempts,
                            final String    email_address,
                            final Timestamp email_address_ts,
                            final boolean   email_address_verified,
                            final Timestamp email_address_verified_ts,
                            final String    password_hash,
                            final Timestamp password_hash_ts,
                            final boolean   delete,
                            final boolean   get,
                            final boolean   head,
                            final boolean   options,
                            final boolean   patch,
                            final boolean   post,
                            final boolean   put) {

        this.role_name                 = system_role_name;
        this.account_id                = account_id;
        this.account_registration_ts   = account_registration_ts;

        this.account_locked            = account_locked;
        this.account_locked_ts         = account_locked_ts;
        this.account_lock_reason       = account_lock_reason;
        this.account_login_attempts    = account_login_attempts;

        this.email_address             = email_address;
        this.email_address_ts          = email_address_ts;
        this.email_address_verified    = email_address_verified;
        this.email_address_verified_ts = email_address_verified_ts;

        this.password_hash             = password_hash;
        this.password_hash_ts          = password_hash_ts;
        this.role_id                   = system_role_id;

        this.delete                    = delete;
        this.get                       = get;
        this.head                      = head;
        this.options                   = options;
        this.patch                     = patch;
        this.post                      = post;
        this.put                       = put;
    }
}
