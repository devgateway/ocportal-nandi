package org.devgateway.toolkit.forms.wicket.page.lists.form;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.wicket.components.table.SelectFilteredBootstrapPropertyColumn;
import org.devgateway.toolkit.forms.wicket.page.lists.AbstractListStatusEntityPage;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniForm;
import org.devgateway.toolkit.persistence.service.category.DepartmentService;
import org.devgateway.toolkit.persistence.service.category.FiscalYearService;

import java.util.List;

/**
 * @author idobre
 * @since 2019-04-02
 */
public abstract class ListAbstractMakueniFormPage<T extends AbstractMakueniForm>
        extends AbstractListStatusEntityPage<T> {
    @SpringBean
    private DepartmentService departmentService;

    @SpringBean
    private FiscalYearService fiscalYearService;

    public ListAbstractMakueniFormPage(final PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected void onInitialize() {
        final Boolean isProcurementPlan = this instanceof ListProcurementPlanPage;
        final List<Department> departments = departmentService.findAll();
        columns.add(new SelectFilteredBootstrapPropertyColumn<>(new Model<>("Department"),
                isProcurementPlan ? "department" : "procurementPlan.department",
                isProcurementPlan ? "department" : "procurementPlan.department",
                new ListModel(departments), dataTable));

        final List<FiscalYear> fiscalYears = fiscalYearService.findAll();
        columns.add(new SelectFilteredBootstrapPropertyColumn<>(new Model<>("Fiscal Years"),
                isProcurementPlan ? "fiscalYear" : "procurementPlan.fiscalYear",
                isProcurementPlan ? "fiscalYear" : "procurementPlan.fiscalYear",
                new ListModel(fiscalYears), dataTable));

        super.onInitialize();
    }
}
