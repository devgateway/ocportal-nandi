/**
 *
 */
package org.devgateway.toolkit.persistence.repository.category;

import org.devgateway.toolkit.persistence.dao.categories.MEStaff;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mpostelnicu
 *
 */
@Transactional
public interface MEStaffRepository extends CategoryRepository<MEStaff> {
}

