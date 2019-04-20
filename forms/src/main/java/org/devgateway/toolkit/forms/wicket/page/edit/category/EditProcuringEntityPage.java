/**
 * 
 */
package org.devgateway.toolkit.forms.wicket.page.edit.category;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.devgateway.toolkit.forms.validators.UniquePropertyEntryValidator;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListProcuringEntityPage;
import org.devgateway.toolkit.persistence.dao.categories.ProcuringEntity;
import org.devgateway.toolkit.persistence.dao.categories.ProcuringEntity_;
import org.devgateway.toolkit.persistence.service.category.ProcuringEntityService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author gmutuhu
 *
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_ADMIN)
@MountPath("/procuringEntity")
public class EditProcuringEntityPage extends AbstractCategoryEditPage<ProcuringEntity> {
    @SpringBean
    private ProcuringEntityService procuringEntityService;

    public EditProcuringEntityPage(final PageParameters parameters) {
        super(parameters);
        jpaService = procuringEntityService;
        listPageClass = ListProcuringEntityPage.class;
    }

   
    private void addUniqueNameValidator() {
        label.getField().add(new UniquePropertyEntryValidator<>(
                getString("uniqueName"),
                procuringEntityService::findOne, (o, v) -> (root, query, cb) -> cb.equal(root.get(
                ProcuringEntity_.label), v), editForm.getModel()
        ));
    }


    @Override
    protected void onInitialize() {
        super.onInitialize();
        addUniqueNameValidator();
        ComponentUtil.addTextField(editForm, "emailAddress").getField().add(EmailAddressValidator.getInstance());
        ComponentUtil.addTextAreaField(editForm, "address");
    }

}