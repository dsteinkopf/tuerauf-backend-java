package net.steinkopf.tuerauf.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Users of the Tür auf App
 */
@Entity
public class User implements Serializable {

    /**
     * Internal ID.
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Incrementing User-ID. Used for Index in Arduino-Pin-Table.
     * Will be -1: 1. before it is set (bug), 2. for dead users.
     */
    @Column(unique = true)
    private int serialId = -1;

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
    @Column(nullable = false)
    private boolean active;

    /**
     * Newly created user?
     */
    @Column(nullable = false)
    private boolean newUser;

    @Column(name = "creation_time", nullable = false, columnDefinition="TIMESTAMP DEFAULT '2015-01-01 01:01:01'")
    private Date creationTime;

    @Column(name = "modification_time", nullable = false, columnDefinition="TIMESTAMP DEFAULT '2015-01-01 01:01:01'")
    private Date modificationTime;


    public User() {
    }

    public User(final String installationId, final String username) {
        super();
        this.installationId = installationId;
        this.username = username;
        this.active = false;
        this.newUser = true;
    }

    @PreUpdate
    public void preUpdate() {
        modificationTime = new Date();
    }

    @PrePersist
    public void prePersist() {
        Date now = new Date();
        creationTime = now;
        modificationTime = now;
    }

    public int getSerialId() {
        return serialId;
    }

    public void setSerialId(final int serialId) {
        this.serialId = serialId;
    }

    public boolean hasSerialId() {
        return serialId >= 0;
    }

    public String getUsername() {
        return username;
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

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(final Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(final Date modificationTime) {
        this.modificationTime = modificationTime;
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
                + "id=" + getId()
                + ",serialId=" + getSerialId()
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
