package org.devgateway.toolkit.persistence.dao.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author gmutuhu
 *
 */

@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(indexes = {@Index(columnList = "procurement_plan_id")})
public class TenderQuotationEvaluation extends AbstractMakueniForm {
    private Date closingDate;
    private Integer numberOfBids;
    
    @OneToOne(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Tender tender;
    
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parent_id")
    @OrderColumn(name = "index")
    private List<Bid> bids = new ArrayList<Bid>();
    
    public Date getClosingDate() {
        return closingDate;
    }

    public Integer getNumberOfBids() {
        return numberOfBids;
    }

    public void setClosingDate(final Date closingDate) {
        this.closingDate = closingDate;
    }

    public void setNumberOfBids(final Integer numberOfBids) {
        this.numberOfBids = numberOfBids;
    }

    public Tender getTender() {
        return tender;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setTender(final Tender tender) {
        this.tender = tender;
    }

    public void setBids(final List<Bid> bids) {
        this.bids = bids;
    }

    @Override
    public void setLabel(final String label) {
    }

    @Override
    public String getLabel() {
        return null;
    }
}