package org.devgateway.toolkit.forms.wicket.page.overview.department;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapBookmarkablePageLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.edit.form.EditProjectPage;
import org.devgateway.toolkit.forms.wicket.page.edit.form.EditPurchaseRequisitionPage;
import org.devgateway.toolkit.forms.wicket.page.overview.AbstractListViewStatus;
import org.devgateway.toolkit.forms.wicket.page.overview.DataEntryBasePage;
import org.devgateway.toolkit.persistence.dao.form.Project;
import org.devgateway.toolkit.persistence.dao.form.PurchaseRequisition;
import org.devgateway.toolkit.persistence.service.filterstate.form.ProjectFilterState;
import org.devgateway.toolkit.persistence.service.form.PurchaseRequisitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author idobre
 * @since 2019-05-24
 */
public class ListViewProjectsOverview extends AbstractListViewStatus<Project> {
    protected static final Logger logger = LoggerFactory.getLogger(DataEntryBasePage.class);

    @SpringBean
    private PurchaseRequisitionService purchaseRequisitionService;

    public ListViewProjectsOverview(final String id, final IModel<List<Project>> model) {
        super(id, model);

        // check if we need to expand a Project
        if (sessionMetadataService.getSessionProject() != null) {
            expandedContainerIds.add(sessionMetadataService.getSessionProject().getId());
        }
    }

    @Override
    protected void populateCompoundListItem(final ListItem<Project> item) {

    }

    @Override
    protected void populateHeader(final String headerFragmentId,
                                  final AjaxLink<Void> header,
                                  final ListItem<Project> item) {
        header.add(AttributeAppender.append("class", "project"));   // add specific class to project overview header

        final Fragment headerFragment = new Fragment(headerFragmentId, "headerFragment", this);
        final Project project = item.getModelObject();

        headerFragment.add(new DeptOverviewStatusLabel("projectStatus", project));
        headerFragment.add(new Label("projectTitle"));
        headerFragment.add(new Label("procurementPlan.fiscalYear"));

        final PageParameters pageParameters = new PageParameters();
        pageParameters.set(WebConstants.PARAM_ID, project.getId());
        final BootstrapBookmarkablePageLink<Void> button = new BootstrapBookmarkablePageLink<>("editProject",
                EditProjectPage.class, pageParameters, Buttons.Type.Success);
        button.add(new TooltipBehavior(Model.of("Edit/View Project")));
        headerFragment.add(button);

        header.add(headerFragment);
    }

    @Override
    protected void populateHideableContainer(final String containerFragmentId,
                                             final TransparentWebMarkupContainer hideableContainer,
                                             final ListItem<Project> item) {
        hideableContainer.add(AttributeAppender.append("class", "tender")); // add specific class to project list
        final Fragment containerFragment = new Fragment(containerFragmentId, "containerFragment", this);
        final Project project = item.getModelObject();


        final BootstrapAjaxLink<Void> addPurchaseRequisition = new BootstrapAjaxLink<Void>("addPurchaseRequisition",
                Buttons.Type.Success) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                sessionMetadataService.setSessionProject(project);
                setResponsePage(EditPurchaseRequisitionPage.class);
            }
        };
        addPurchaseRequisition.setLabel(
                new StringResourceModel("addPurchaseRequisition", ListViewProjectsOverview.this, null));
        containerFragment.add(addPurchaseRequisition);
        addPurchaseRequisition.setVisibilityAllowed(
                ComponentUtil.canAccessAddNewButtonInDeptOverview(sessionMetadataService));

        long startTime = System.nanoTime();

        final List<PurchaseRequisition> purchaseRequisitions = purchaseRequisitionService.findByProject(project);

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1000000000.0;
        // logger.info("------- [DepartmentPage] Fetch " + purchaseRequisitions.size() + " PRs in: " + duration);
        // logger.info("-------------------------------------------------------------------------------");

        final ListViewPurchaseRequisitionOverview listViewPurchaseRequisitionOverview =
                new ListViewPurchaseRequisitionOverview("purchaseReqOverview", new ListModel<>(purchaseRequisitions));
        containerFragment.add(listViewPurchaseRequisitionOverview);

        hideableContainer.add(containerFragment);
    }

    @Override
    protected Long getItemId(final ListItem<Project> item) {
        return item.getModelObject().getId();
    }
}
