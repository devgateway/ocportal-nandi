/*******************************************************************************
 * Copyright (c) 2015 Development Gateway, Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/MIT
 *
 * Contributors:
 * Development Gateway - initial API and implementation
 *******************************************************************************/
package org.devgateway.toolkit.forms.wicket.page.lists.category;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.wicket.page.edit.category.EditProcurementMethodRationalePage;
import org.devgateway.toolkit.persistence.dao.categories.ProcurementMethodRationale;
import org.devgateway.toolkit.persistence.service.category.ProcurementMethodRationaleService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_ADMIN)
@MountPath
public class ListProcurementMethodRationalePage extends AbstractListCategoryPage<ProcurementMethodRationale> {

    @SpringBean
    protected ProcurementMethodRationaleService departmentService;

    public ListProcurementMethodRationalePage(final PageParameters pageParameters) {
        super(pageParameters);

        this.jpaService = departmentService;
        this.editPageClass = EditProcurementMethodRationalePage.class;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
    }


}
