package org.devgateway.toolkit.persistence.service.filterstate.form;

import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.devgateway.toolkit.persistence.dao.form.AwardAcceptance;
import org.devgateway.toolkit.persistence.dao.form.AwardAcceptance_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gmutuhu
 */
public class AwardAcceptanceFilterState extends AbstractPurchaseReqMakueniFilterState<AwardAcceptance> {
    protected Supplier awardee;

    @Override
    public Specification<AwardAcceptance> getSpecification() {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (awardee != null) {
                predicates.add(cb.equal(root.get(AwardAcceptance_.awardee), awardee));
            }

            predicates.add(super.getSpecification().toPredicate(root, query, cb));
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
