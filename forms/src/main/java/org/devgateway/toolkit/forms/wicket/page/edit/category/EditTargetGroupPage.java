package org.devgateway.toolkit.forms.wicket.page.edit.category;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.validators.UniquePropertyEntryValidator;
import org.devgateway.toolkit.persistence.dao.categories.ChargeAccount_;
import org.devgateway.toolkit.persistence.dao.categories.TargetGroup_;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListTargetGroupPage;
import org.devgateway.toolkit.persistence.dao.categories.TargetGroup;
import org.devgateway.toolkit.persistence.service.category.TargetGroupService;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author idobre
 * @since 2019-03-11
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_ADMIN)
@MountPath("/targetgroup")
public class EditTargetGroupPage extends AbstractCategoryEditPage<TargetGroup> {
    @SpringBean
    private TargetGroupService targetGroupService;

    public EditTargetGroupPage(final PageParameters parameters) {
        super(parameters);
        jpaService = targetGroupService;
        listPageClass = ListTargetGroupPage.class;
    }

    private void addUniqueNameValidator() {
        label.getField().add(new UniquePropertyEntryValidator<>(
                getString("uniqueName"),
                targetGroupService::findOne,
                (o, v) -> (root, query, cb) -> cb.equal(root.get(TargetGroup_.label), v),
                editForm.getModel()
        ));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        addUniqueNameValidator();
    }
}
