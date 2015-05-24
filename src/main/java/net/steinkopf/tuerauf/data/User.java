package net.steinkopf.tuerauf.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Users of the Tür auf App
 */
@Entity
public class User {

    /**
     * Incrementing User-ID.
     */
    @Id
    @GeneratedValue
    private Long serialId;

    /**
     * Display name.
     */
    @Column(nullable = false)
    private String username;

    /**
     * May the user use his/her account?
     */
    @Column
    private boolean active;

    /**
     * Newly created user?
     */
    @Column
    private boolean newUser;


    protected User() {
    }

    public User(final String username) {
        super();
        this.username = username;
        this.active = false;
        this.newUser = true;
    }

    public Long getSerialId() {
        return serialId;
    }

    public void setSerialId(final Long serialId) {
        this.serialId = serialId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
        markAsChanged();
    }

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(final boolean newUser) {
        this.newUser = newUser;
    }


    private void markAsChanged() {
        this.setNewUser(false);
        // TODO usernameOld = null, pinOld = null
    }


    @Override
    public String toString() {
        return getUsername()
                + ":serialId=" + getSerialId()
                + ",active=" + isActive()
                + ",newUser=" + isNewUser();
    }
}
