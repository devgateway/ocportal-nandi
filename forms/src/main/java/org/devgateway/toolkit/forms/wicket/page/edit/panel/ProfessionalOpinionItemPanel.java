package org.devgateway.toolkit.forms.wicket.page.edit.panel;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.RangeValidator;
import org.devgateway.toolkit.forms.validators.BigDecimalValidator;
import org.devgateway.toolkit.forms.wicket.components.ListViewSectionPanel;
import org.devgateway.toolkit.forms.wicket.components.form.BootstrapAddButton;
import org.devgateway.toolkit.forms.wicket.components.form.FileInputBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.Select2ChoiceBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.TextFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.providers.GenericChoiceProvider;
import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.devgateway.toolkit.persistence.dao.form.ProfessionalOpinion;
import org.devgateway.toolkit.persistence.dao.form.ProfessionalOpinionItem;

import java.math.BigDecimal;

/**
 * @author idobre
 * @since 2019-04-17
 */
public class ProfessionalOpinionItemPanel extends ListViewSectionPanel<ProfessionalOpinionItem, ProfessionalOpinion> {

    public ProfessionalOpinionItemPanel(final String id) {
        super(id);
    }

    protected class ListItemsValidator implements IFormValidator {
        @Override
        public FormComponent<?>[] getDependentFormComponents() {
            return new FormComponent[0];
        }

        @Override
        public void validate(Form<?> form) {
//            final Set<PlanItem> planItems = new HashSet<>();
//            final List<PurchaseItem> purchaseItems = PRequisitionPanel.this.getModelObject();
//            for (final PurchaseItem purchaseItem : purchaseItems) {
//                if (purchaseItem.getPlanItem() != null) {
//                    planItems.add(purchaseItem.getPlanItem());
//                }
//            }
//
//            if (purchaseItems.size() != 0 && purchaseItems.size() != planItems.size()) {
//                final ListView<PurchaseItem> list = (ListView<PurchaseItem>) PRequisitionPanel.this
//                        .get("listWrapper").get("list");
//                if (list != null) {
//                    for (int i = 0; i < list.size(); i++) {
//                        final TransparentWebMarkupContainer accordion =
//                                (TransparentWebMarkupContainer) list.get("" + i).get(ID_ACCORDION);
//
//                        final GenericBootstrapFormComponent planItem =
//                                (GenericBootstrapFormComponent) accordion.get(ID_ACCORDION_TOGGLE)
//                                        .get("headerField").get("planItem");
//
//                        if (planItem != null) {
//                            planItem.getField().error(getString("uniqueItem"));
//
//                          final Optional<AjaxRequestTarget> target = RequestCycle.get().find(AjaxRequestTarget.class);
//                            if (target.isPresent()) {
//                                target.get().add(planItem.getBorder());
//                            }
//                        }
//                    }
//                }
//            }
        }
    }

    @Override
    protected BootstrapAddButton getAddNewChildButton() {
        return new AddNewChildButton("newButton", Model.of("New Professional Opinion"));
    }


    @Override
    protected void onInitialize() {
        super.onInitialize();

        final Form form = (Form) getParent();
        if (form != null) {
            form.add(new ListItemsValidator());
        }
    }

    @Override
    public ProfessionalOpinionItem createNewChild(final IModel<ProfessionalOpinion> parentModel) {
        final ProfessionalOpinionItem child = new ProfessionalOpinionItem();
        child.setParent(parentModel.getObject());
        child.setExpanded(true);
        child.setEditable(true);
        return child;
    }

    @Override
    public void populateCompoundListItem(final ListItem<ProfessionalOpinionItem> item) {
        Select2ChoiceBootstrapFormComponent<Supplier> awardeeSelector =
                new Select2ChoiceBootstrapFormComponent<>(
                "awardee",
                        new GenericChoiceProvider<>(ComponentUtil.getSuppliersInTenderQuotation(
                                item.getModelObject().getParent().getTenderProcess(), false))
        );
        awardeeSelector.required();
        item.add(awardeeSelector);

        ComponentUtil.addDateField(item, "professionalOpinionDate").required();

        final TextFieldBootstrapFormComponent<BigDecimal> recommendedAwardAmount =
                ComponentUtil.addBigDecimalField(item, "recommendedAwardAmount");
        recommendedAwardAmount.required();
        recommendedAwardAmount.getField().add(RangeValidator.minimum(BigDecimal.ZERO), new BigDecimalValidator());

        ComponentUtil.addDateField(item, "approvedDate").required();

        final FileInputBootstrapFormComponent formDocs = new FileInputBootstrapFormComponent("formDocs");
        formDocs.required();
        item.add(formDocs);
    }

    @Override
    protected boolean filterListItem(final ProfessionalOpinionItem purchaseItem) {
        return true;
    }


}
