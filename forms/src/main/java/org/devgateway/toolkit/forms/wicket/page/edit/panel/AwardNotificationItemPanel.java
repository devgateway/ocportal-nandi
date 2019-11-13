package org.devgateway.toolkit.forms.wicket.page.edit.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.RangeValidator;
import org.devgateway.toolkit.forms.validators.AfterThanDateValidator;
import org.devgateway.toolkit.forms.validators.BigDecimalValidator;
import org.devgateway.toolkit.forms.wicket.components.ListViewSectionPanel;
import org.devgateway.toolkit.forms.wicket.components.form.BootstrapAddButton;
import org.devgateway.toolkit.forms.wicket.components.form.DateFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.FileInputBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.Select2ChoiceBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.providers.GenericChoiceProvider;
import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.devgateway.toolkit.persistence.dao.form.AwardNotification;
import org.devgateway.toolkit.persistence.dao.form.AwardNotificationItem;
import org.devgateway.toolkit.persistence.dao.form.Tender;

import java.math.BigDecimal;

/**
 * @author mpostelnicu
 */
public class AwardNotificationItemPanel extends ListViewSectionPanel<AwardNotificationItem, AwardNotification> {

    private GenericSleepFormComponent<String> supplierID;
    private GenericSleepFormComponent<String> supplierAddress;
    private Select2ChoiceBootstrapFormComponent<Supplier> awardeeSelector;

    public AwardNotificationItemPanel(final String id) {
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
        return new AddNewChildButton("newButton", Model.of("New Award Notification"));
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
    public AwardNotificationItem createNewChild(final IModel<AwardNotification> parentModel) {
        final AwardNotificationItem child = new AwardNotificationItem();
        child.setParent(parentModel.getObject());
        child.setExpanded(true);
        child.setEditable(true);
        return child;
    }

    class AwardeeAjaxComponentUpdatingBehavior extends AjaxFormComponentUpdatingBehavior {
        AwardeeAjaxComponentUpdatingBehavior(final String event) {
            super(event);
        }

        @Override
        protected void onUpdate(final AjaxRequestTarget target) {
            target.add(supplierID);
            target.add(supplierAddress);
        }
    }

    @Override
    public void populateCompoundListItem(final ListItem<AwardNotificationItem> item) {
        ComponentUtil.addBigDecimalField(item, "awardValue").required()
                .getField().add(RangeValidator.minimum(BigDecimal.ZERO), new BigDecimalValidator());

        final DateFieldBootstrapFormComponent awardDate = ComponentUtil.addDateField(item, "awardDate");
        awardDate.required();
        final Tender tender = item.getModelObject().getParent().getTenderProcess().getSingleTender();
        if (tender != null && tender.getInvitationDate() != null) {
            awardDate.getField().add(new AfterThanDateValidator(tender.getInvitationDate()));
        }

        ComponentUtil.addIntegerTextField(item, "acknowledgementDays")
                .getField().add(RangeValidator.minimum(0));

        addSupplierInfo(item);

        final FileInputBootstrapFormComponent formDocs = new FileInputBootstrapFormComponent("formDocs");
        formDocs.required();
        item.add(formDocs);
    }

    private void addSupplierInfo(ListItem<AwardNotificationItem> item) {
        awardeeSelector = new Select2ChoiceBootstrapFormComponent<>(
                "awardee",
                new GenericChoiceProvider<>(
                        ComponentUtil.getSuppliersInTenderQuotation(
                                item.getModelObject().getParent().getTenderProcess(), false))
        );
        awardeeSelector.required();
        awardeeSelector.getField().add(new AwardeeAjaxComponentUpdatingBehavior("change"));
        item.add(awardeeSelector);

        supplierID = new GenericSleepFormComponent<>("supplierID", (IModel<String>) () -> {
            if (awardeeSelector.getModelObject() != null) {
                return awardeeSelector.getModelObject().getCode();
            }
            return null;
        });
        supplierID.setOutputMarkupId(true);
        item.add(supplierID);

        supplierAddress = new GenericSleepFormComponent<>("supplierAddress", (IModel<String>) () -> {
            if (awardeeSelector.getModelObject() != null) {
                return awardeeSelector.getModelObject().getAddress();
            }
            return null;
        });
        supplierAddress.setOutputMarkupId(true);
        item.add(supplierAddress);
    }


    @Override
    protected boolean filterListItem(final AwardNotificationItem purchaseItem) {
        return true;
    }


}
