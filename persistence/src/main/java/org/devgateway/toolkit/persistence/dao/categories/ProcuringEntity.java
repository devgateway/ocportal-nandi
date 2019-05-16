package org.devgateway.toolkit.persistence.dao.categories;

import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author gmutuhu
 */
@Entity
@Audited
public class ProcuringEntity extends Category {
    @Column(length = DBConstants.MAX_DEFAULT_TEXT_LENGTH_ONE_LINE)
    private String address;
    private String emailAddress;

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
