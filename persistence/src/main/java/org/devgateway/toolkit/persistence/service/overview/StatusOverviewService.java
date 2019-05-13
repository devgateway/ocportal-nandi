package org.devgateway.toolkit.persistence.service.overview;

import java.util.List;

import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dto.DepartmentOverviewData;

/**
 * @author gmutuhu
 */
public interface StatusOverviewService {   
    List<DepartmentOverviewData> getProjectsByDepartment(Long fiscaYearId);    
}
