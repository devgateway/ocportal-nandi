package org.devgateway.toolkit.persistence.dao.alerts;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.Transient;

import javax.persistence.Entity;

/**
 * @author idobre
 * @since 2019-08-21
 * <p>
 * The class encapsulates statistics of email alerts processing.
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Audited
// @Table(indexes = {@Index(columnList = "email")})
public class AlertsStatistics {
    private Integer numberSentAlerts = 0;

    // all time values are in milliseconds
    private Long dbTime = 0L;

    private Long creationTime = 0L;

    private Long sendingTime = 0L;

    // temporary fields
    @Transient
    private transient Long dbTimeStart = 0L;

    @Transient
    private transient Long creationTimeStart = 0L;

    @Transient
    private transient Long sendingTimeStart = 0L;

    public void addStats(final AlertsStatistics stats) {
        this.dbTime += stats.getDbTime();
        this.creationTime += stats.getCreationTime();
        this.sendingTime += stats.getSendingTime();
        this.numberSentAlerts += stats.getNumberSentAlerts();
    }

    public long getTotalProcessingTime() {
        return dbTime + creationTime + sendingTime;
    }

    /**
     * Marks DB access
     */
    public void startDbAccess() {
        this.dbTimeStart = System.currentTimeMillis();
    }

    /**
     * Marks end of DB access
     */
    public void endDbAccess() {
        if (this.dbTimeStart != 0) {
            this.dbTime += System.currentTimeMillis() - this.dbTimeStart;
            this.dbTimeStart = 0L;
        }
    }

    /**
     * Marks message creation processing
     */
    public void startCreationStage() {
        this.creationTimeStart = System.currentTimeMillis();
    }

    /**
     * Marks end of message creation processing
     */
    public void endCreationStage() {
        if (this.creationTimeStart != 0) {
            this.creationTime += System.currentTimeMillis() - this.creationTimeStart;
            this.creationTimeStart = 0L;
        }
    }

    /**
     * Marks message creation processing
     */
    public void startSendingStage() {
        this.sendingTimeStart = System.currentTimeMillis();
    }

    /**
     * Marks end of message creation processing
     */
    public void endSendingStage() {
        if (this.sendingTimeStart != 0) {
            this.sendingTime += System.currentTimeMillis() - this.sendingTimeStart;
            this.sendingTimeStart = 0L;
        }
    }

    public Integer getNumberSentAlerts() {
        return numberSentAlerts;
    }

    public void setNumberSentAlerts(final Integer numberSentAlerts) {
        this.numberSentAlerts = numberSentAlerts;
    }

    public Long getDbTime() {
        return dbTime;
    }

    public void setDbTime(final Long dbTime) {
        this.dbTime = dbTime;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(final Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getSendingTime() {
        return sendingTime;
    }

    public void setSendingTime(final Long sendingTime) {
        this.sendingTime = sendingTime;
    }
}
