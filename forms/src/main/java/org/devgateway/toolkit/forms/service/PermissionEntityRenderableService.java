package org.devgateway.toolkit.forms.service;

import org.devgateway.toolkit.persistence.dao.AbstractStatusAuditableEntity;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniForm;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.web.WebSecurityUtil;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author idobre
 * @since 2019-04-23
 */
@Service
public class PermissionEntityRenderableService {

    public String getAllowedAccess(final AbstractStatusAuditableEntity entity) {
        final Set<String> roles = WebSecurityUtil.getStringRolesForCurrentPerson();

        // admins can edit
        if (roles.contains(SecurityConstants.Roles.ROLE_ADMIN)) {
            return SecurityConstants.Action.EDIT;
        }

        // new forms can be added by validator/users in addition to admin types
        if (entity.isNew() && (roles.contains(SecurityConstants.Roles.ROLE_VALIDATOR)
                || roles.contains(SecurityConstants.Roles.ROLE_USER))) {
            return SecurityConstants.Action.EDIT;
        }

        // T should extend AbstractMakueniForm
        if (!entity.isNew() && (roles.contains(SecurityConstants.Roles.ROLE_VALIDATOR)
                || roles.contains(SecurityConstants.Roles.ROLE_USER))) {
            if (entity instanceof AbstractMakueniForm) {
                final ProcurementPlan procurementPlan = ((AbstractMakueniForm) entity).getProcurementPlan();

                if (procurementPlan == null || procurementPlan.getDepartment() == null) {
                    return SecurityConstants.Action.VIEW;
                } else {
                    final Department department = WebSecurityUtil.getCurrentAuthenticatedPerson().getDepartment();
                    final Department formDepartment = procurementPlan.getDepartment();

                    // users with different department should be redirected to view mode.
                    if (department.equals(formDepartment)) {
                        return SecurityConstants.Action.EDIT;
                    } else {
                        return SecurityConstants.Action.VIEW;
                    }
                }
            }
        }

        return null;
    }
}