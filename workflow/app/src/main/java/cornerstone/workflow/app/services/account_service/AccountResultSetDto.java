package cornerstone.workflow.app.services.account_service;

import java.sql.Timestamp;

public class AccountResultSetDto {
    private int account_id;
    private Timestamp account_registration_ts;
    private boolean account_available;
    private Timestamp account_available_ts;
    private String account_disable_reason;
    private String email_address;
    private Timestamp email_address_ts;
    private boolean email_address_verified;
    private Timestamp email_address_verified_ts;
    private String password_hash;
    private Timestamp password_hash_ts;

    public AccountResultSetDto() {
    }

    public int getAccount_id() {
        return account_id;
    }

    public void set_account_id(int account_id) {
        this.account_id = account_id;
    }

    public Timestamp getAccount_registration_ts() {
        return account_registration_ts;
    }

    public void set_account_registration_ts(Timestamp account_registration_ts) {
        this.account_registration_ts = account_registration_ts;
    }

    public boolean isAccount_available() {
        return account_available;
    }

    public void set_account_available(boolean account_available) {
        this.account_available = account_available;
    }

    public Timestamp getAccount_available_ts() {
        return account_available_ts;
    }

    public void set_account_available_ts(Timestamp account_available_ts) {
        this.account_available_ts = account_available_ts;
    }

    public String getAccount_disable_reason() {
        return account_disable_reason;
    }

    public void set_account_disable_reason(String account_disable_reason) {
        this.account_disable_reason = account_disable_reason;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void set_email_address(String email_address) {
        this.email_address = email_address;
    }

    public Timestamp getEmail_address_ts() {
        return email_address_ts;
    }

    public void set_email_address_ts(Timestamp email_address_ts) {
        this.email_address_ts = email_address_ts;
    }

    public boolean isEmail_address_verified() {
        return email_address_verified;
    }

    public void set_email_address_verified(boolean email_address_verified) {
        this.email_address_verified = email_address_verified;
    }

    public Timestamp getEmail_address_verified_ts() {
        return email_address_verified_ts;
    }

    public void set_email_address_verified_ts(Timestamp email_address_verified_ts) {
        this.email_address_verified_ts = email_address_verified_ts;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void set_password_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public Timestamp getPassword_hash_ts() {
        return password_hash_ts;
    }

    public void set_password_hash_ts(Timestamp password_hash_ts) {
        this.password_hash_ts = password_hash_ts;
    }

    @Override
    public String toString() {
        return "AccountResultSetDto{" +
                "account_id=" + account_id +
                ", account_registration_ts=" + account_registration_ts +
                ", account_available=" + account_available +
                ", account_available_ts=" + account_available_ts +
                ", account_disable_reason='" + account_disable_reason + '\'' +
                ", email_address='" + email_address + '\'' +
                ", email_address_ts=" + email_address_ts +
                ", email_address_verified=" + email_address_verified +
                ", email_address_verified_ts=" + email_address_verified_ts +
                ", password_hash='" + password_hash + '\'' +
                ", password_hash_ts=" + password_hash_ts +
                '}';
    }
}
