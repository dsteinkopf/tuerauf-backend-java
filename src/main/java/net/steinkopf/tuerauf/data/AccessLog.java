package net.steinkopf.tuerauf.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;


/**
 * Record user's access.
 */
@Entity
@Table(indexes = { @Index(columnList = "accessType") })
public class AccessLog {


    public enum AccessType {checkLocation, openDoor}


    /**
     * Internal ID.
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * The accessing user.
     */
    @ManyToOne(optional = false)
    @JoinColumn(referencedColumnName = "serialId")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    @Column
    private Double geoy;

    @Column
    private Double geox;

    @Column(nullable = false, columnDefinition="TIMESTAMP")
    private Date accessTimestamp;

    @Column
    private String result;


    @Override
    public String toString() {
        return "AccessLog{" +
                "accessTimestamp=" + accessTimestamp +
                ", accessType=" + accessType +
                ", user=" + user +
                ", geox=" + geox +
                ", geoy=" + geoy +
                ", result=" + result +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AccessLog)) return false;

        final AccessLog accessLog = (AccessLog) o;

        if (!id.equals(accessLog.id)) return false;
        if (!user.equals(accessLog.user)) return false;
        if (geoy != null ? !geoy.equals(accessLog.geoy) : accessLog.geoy != null) return false;
        if (geox != null ? !geox.equals(accessLog.geox) : accessLog.geox != null) return false;
        //noinspection SimplifiableIfStatement
        if (accessType != accessLog.accessType) return false;
        return accessTimestamp.equals(accessLog.accessTimestamp);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + (geoy != null ? geoy.hashCode() : 0);
        result = 31 * result + (geox != null ? geox.hashCode() : 0);
        result = 31 * result + accessType.hashCode();
        result = 31 * result + accessTimestamp.hashCode();
        return result;
    }

    @SuppressWarnings("unused")
    public Long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(final Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Double getGeoy() {
        return geoy;
    }

    public void setGeoy(final Double geoy) {
        this.geoy = geoy;
    }

    public Double getGeox() {
        return geox;
    }

    public void setGeox(final Double geox) {
        this.geox = geox;
    }

    @SuppressWarnings("unused")
    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(final AccessType accessType) {
        this.accessType = accessType;
    }

    @SuppressWarnings("unused")
    public Date getAccessTimestamp() {
        return accessTimestamp;
    }

    public void setAccessTimestamp(final Date accessTimestamp) {
        this.accessTimestamp = accessTimestamp;
    }

    @SuppressWarnings("unused")
    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }
}
