package cornerstone.workflow.app.services.account_service;

import java.sql.Timestamp;

public class AccountResultSetDto {
    private int account_id;
    private Timestamp account_registration_ts;
    private boolean account_locked;
    private Timestamp account_locked_ts;
    private String account_lock_reason;
    private int account_login_attempts;
    private String email_address;
    private Timestamp email_address_ts;
    private boolean email_address_verified;
    private Timestamp email_address_verified_ts;
    private String password_hash;
    private Timestamp password_hash_ts;

    public AccountResultSetDto() {
    }

    public int get_account_id() {
        return account_id;
    }

    public void set_account_id(final int account_id) {
        this.account_id = account_id;
    }


    public Timestamp get_account_registration_ts() {
        return account_registration_ts;
    }

    public void set_account_registration_ts(final Timestamp account_registration_ts) {
        this.account_registration_ts = account_registration_ts;
    }


    public boolean get_account_locked() {
        return account_locked;
    }

    public void set_account_locked(boolean locked) {
        this.account_locked = locked;
    }


    public Timestamp get_account_locked_ts() {
        return account_locked_ts;
    }

    public void set_account_locked_ts(final Timestamp account_locked_ts) {
        this.account_locked_ts = account_locked_ts;
    }


    public String get_account_lock_reason() {
        return account_lock_reason;
    }

    public void set_account_lock_reason(final String account_lock_reason) {
        this.account_lock_reason = account_lock_reason;
    }

    public int get_account_login_attempts() {
        return account_login_attempts;
    }

    public void set_account_login_attempts(final int account_login_attempts) {
        this.account_login_attempts = account_login_attempts;
    }

    public String get_email_address() {
        return email_address;
    }

    public void set_email_address(final String email_address) {
        this.email_address = email_address;
    }


    public Timestamp get_email_address_ts() {
        return email_address_ts;
    }

    public void set_email_address_ts(final Timestamp email_address_ts) {
        this.email_address_ts = email_address_ts;
    }


    public boolean get_email_address_verified() {
        return email_address_verified;
    }

    public void set_email_address_verified(boolean email_address_verified) {
        this.email_address_verified = email_address_verified;
    }


    public Timestamp get_email_address_verified_ts() {
        return email_address_verified_ts;
    }

    public void set_email_address_verified_ts(final Timestamp email_address_verified_ts) {
        this.email_address_verified_ts = email_address_verified_ts;
    }


    public String get_password_hash() {
        return password_hash;
    }

    public void set_password_hash(final String password_hash) {
        this.password_hash = password_hash;
    }


    public Timestamp get_password_hash_ts() {
        return password_hash_ts;
    }

    public void set_password_hash_ts(final Timestamp password_hash_ts) {
        this.password_hash_ts = password_hash_ts;
    }
}
