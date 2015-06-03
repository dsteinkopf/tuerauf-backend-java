package net.steinkopf.tuerauf.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Users of the Tür auf App
 */
@Entity
public class User {

    /**
     * Internal ID.
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Incrementing User-ID. Used for Index in Arduino-Pin-Table.
     */
    @Column(unique = true, nullable = false)
    @NotNull
    private int serialId;

    /**
     * "Secret key" for identifying a user.
     */
    @Column(unique = true, nullable = false)
    @NotNull
    private String installationId;

    /**
     * Display name.
     */
    @Column(unique = true, length = 20, nullable = false)
    @Size(min = 2, max = 20)
    @NotNull
    private String username;

    /**
     * Display name before last change.
     */
    @Size(min = 2, max = 20)
    @Column(length = 20)
    private String usernameOld;

    /**
     * PIN for opening the door. This will be transferred to arduino and NOT be stored here then.
     */
    @Size(min = 4, max = 4)
    @Column(length = 4)
    private String pin;

    /**
     * PIN before last change.
     */
    @Size(min = 4, max = 4)
    @Column(length = 4)
    private String pinOld;

    /**
     * May the user use his/her account and open the door?
     */
    @Column
    private boolean active;

    /**
     * Newly created user?
     */
    @Column
    private boolean newUser;


    public User() {
    }

    public User(final String installationId) {
        super();
        this.installationId = installationId;
        this.active = false;
        this.newUser = true;
    }

    public int getSerialId() {
        return serialId;
    }

    public void setSerialId(final int serialId) {
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
        setNewUser(false);
        markAsChanged();
    }

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(final boolean newUser) {
        this.newUser = newUser;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(final String installationId) {
        this.installationId = installationId;
    }

    public String getUsernameOld() {
        return usernameOld;
    }

    public void setUsernameOld(final String usernameOld) {
        this.usernameOld = usernameOld;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(final String pin) {
        this.pin = pin;
    }

    public String getPinOld() {
        return pinOld;
    }

    public void setPinOld(final String pinOld) {
        this.pinOld = pinOld;
    }

    private void markAsChanged() {
        this.setUsernameOld(null);
        this.setPinOld(null);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return getUsername()
                + ":{"
                + "serialId=" + getSerialId()
                + ",active=" + isActive()
                + ",newUser=" + isNewUser()
                + "}";
    }

    @Override
    public boolean equals(Object objToCompare) {

        if (!(objToCompare instanceof User)) {
            return false;
        }
        if (this.getId() == null) {
            return false;
        }
        final User userToCompare = (User) objToCompare;
        return this.getId().equals(userToCompare.getId());
    }

    public void updateData(final String usernameNew, final String pinNew) {
        if (!usernameNew.equals(this.username)) {
            // real change of value
            if (this.usernameOld == null) {
                this.usernameOld = this.username;
            }
            this.username = usernameNew;
        }
        if (!pinNew.equals(this.pin)) {
            // real change of value
            if (this.pinOld == null) {
                this.pinOld = this.pin;
            }
            this.pin = pinNew;
        }
    }
}
