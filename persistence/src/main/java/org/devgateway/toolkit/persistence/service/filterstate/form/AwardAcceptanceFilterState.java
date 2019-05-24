package org.devgateway.toolkit.persistence.service.filterstate.form;

import org.devgateway.toolkit.persistence.dao.form.AwardAcceptance;
import org.devgateway.toolkit.persistence.service.filterstate.StatusAuditableEntityFilterState;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gmutuhu
 */
public class AwardAcceptanceFilterState extends AbstractPurchaseReqMakueniFilterState<AwardAcceptance> {
    @Override
    public Specification<AwardAcceptance> getSpecification() {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            predicates.add(super.getSpecification().toPredicate(root, query, cb));
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}