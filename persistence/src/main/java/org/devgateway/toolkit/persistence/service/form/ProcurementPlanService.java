package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.service.BaseJpaService;

/**
 * @author idobre
 * @since 2019-04-02
 */
public interface ProcurementPlanService extends BaseJpaService<ProcurementPlan> {
    Long countByDepartmentAndFiscalYearAndIdNot(Department department, FiscalYear fiscalYear, Long id);
}