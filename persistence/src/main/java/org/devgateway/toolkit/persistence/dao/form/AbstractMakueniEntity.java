package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.devgateway.toolkit.persistence.dao.AbstractAuditableEntity;
import org.devgateway.toolkit.persistence.dao.AbstractStatusAuditableEntity;
import org.devgateway.toolkit.persistence.dao.FileMetadata;
import org.devgateway.toolkit.persistence.dao.Labelable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author idobre
 * @since 2019-04-01
 */
@MappedSuperclass
public abstract class AbstractMakueniEntity extends AbstractStatusAuditableEntity implements Labelable {

    private Date approvedDate;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<FileMetadata> formDocs;


    public abstract Collection<? extends AbstractMakueniEntity> getDirectChildrenEntities();

    @Transactional
    @JsonIgnore
    public Collection<? extends AbstractMakueniEntity> getAllChildrenInHierarchy() {
        Set<? extends AbstractMakueniEntity> collect = getDirectChildrenEntities().stream()
                .flatMap(s -> s.getDirectChildrenEntities().stream())
                .collect(Collectors.toSet());
        System.out.println(collect);
        return collect;
    }

    @Override
    public AbstractAuditableEntity getParent() {
        return null;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(final Date approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Set<FileMetadata> getFormDocs() {
        return formDocs;
    }

    public void setFormDocs(final Set<FileMetadata> formDocs) {
        this.formDocs = formDocs;
    }
}
