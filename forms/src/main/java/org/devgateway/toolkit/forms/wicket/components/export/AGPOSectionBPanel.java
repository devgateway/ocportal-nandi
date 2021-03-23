package org.devgateway.toolkit.forms.wicket.components.export;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.wicket.components.form.Select2ChoiceBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.providers.GenericChoiceProvider;
import org.devgateway.toolkit.persistence.dto.NamedDateRange;
import org.devgateway.toolkit.persistence.service.category.FiscalYearService;
import org.devgateway.toolkit.persistence.service.excel.AGPOSectionBExporter;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Octavian Ciubotaru
 */
public class AGPOSectionBPanel extends AbstractReportPanel<AGPOSectionBPanel.Filter> {

    public static class Filter implements Serializable {

        private NamedDateRange dateRange;

        public NamedDateRange getDateRange() {
            return dateRange;
        }

        public void setDateRange(NamedDateRange dateRange) {
            this.dateRange = dateRange;
        }
    }

    @SpringBean
    private FiscalYearService fiscalYearService;

    @SpringBean
    private AGPOSectionBExporter exporter;

    public AGPOSectionBPanel(String id, AjaxFormListener ajaxFormListener) {
        super(id, ajaxFormListener, Model.of(new Filter()));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        Select2ChoiceBootstrapFormComponent<NamedDateRange> dateRange =
                new Select2ChoiceBootstrapFormComponent<>("dateRange",
                new GenericChoiceProvider<>(fiscalYearService.createSixMonthDateRangesForAllFiscalYears()));
        getDataExportForm().add(dateRange);
    }

    @Override
    protected String getFileName() {
        return "AGPO Section B: All Contracts Awards to the Target Group.xlsx";
    }

    @Override
    protected boolean hasData() {
        Filter filter = getDataExportForm().getModelObject();
        return filter.getDateRange() != null
                && exporter.hasData(filter.getDateRange());
    }

    @Override
    protected void export(IResource.Attributes attributes) throws IOException {
        Filter filter = getDataExportForm().getModelObject();
        XSSFWorkbook workbook = exporter.export(filter.getDateRange());
        workbook.write(attributes.getResponse().getOutputStream());
    }
}
