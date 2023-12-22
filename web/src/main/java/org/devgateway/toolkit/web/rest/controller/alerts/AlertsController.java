package org.devgateway.toolkit.web.rest.controller.alerts;

import io.swagger.v3.oas.annotations.Operation;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.devgateway.toolkit.persistence.dao.alerts.Alert;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.Item;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.service.alerts.AlertService;
import org.devgateway.toolkit.persistence.service.category.DepartmentService;
import org.devgateway.toolkit.persistence.service.category.ItemService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author idobre
 * @since 23/08/2019
 */
@RestController
public class AlertsController {
    private static final Logger logger = LoggerFactory.getLogger(AlertsController.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private TenderProcessService tenderProcessService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AlertsEmailService alertsEmailService;

    @Operation(summary = "Subscribe to a particular Email Alert")
    @RequestMapping(value = "/api/makueni/alerts/subscribeAlert",
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = "application/json")
    @Transactional
    public Map subscribeAlert(@ModelAttribute @Valid final AlertsRequest alertsRequest) {
        final Map<String, Object> response = new HashMap();
        try {
            final TenderProcess purchaseReq;
            if (alertsRequest.getPurchaseReqId() != null) {
                final Optional<TenderProcess> purchaseReqOptional = tenderProcessService
                        .findById(alertsRequest.getPurchaseReqId());

                if (purchaseReqOptional.isPresent()) {
                    purchaseReq = purchaseReqOptional.get();
                } else {
                    response.put("status", false);
                    response.put("message", "You are trying to subscribe to a non existing Tender");
                    return response;
                }
            } else {
                // we should subscribe to at least 1 Department or 1 Item
                if (CollectionUtils.isEmpty(alertsRequest.getDepartments())
                        && CollectionUtils.isEmpty(alertsRequest.getItems())) {

                    response.put("status", false);
                    response.put("message", "Please subscribe to at least 1 Department or 1 Item");
                    return response;
                }

                purchaseReq = null;
            }

            final Set<Department> departments;
            final Set<Item> items;

            if (CollectionUtils.isNotEmpty(alertsRequest.getDepartments())) {
                departments = departmentService.findAllById(alertsRequest.getDepartments());
            } else {
                departments = new HashSet<>();
            }

            if (CollectionUtils.isNotEmpty(alertsRequest.getItems())) {
                items = itemService.findAllById(alertsRequest.getItems());
            } else {
                items = new HashSet<>();
            }

            // check that this user doesn't have a similar Alert.
            final List<Alert> existingAlerts = alertService.findByEmail(alertsRequest.getEmail());
            if (!existingAlerts.isEmpty()) {
                for (final Alert item : existingAlerts) {
                    if (CollectionUtils.isEqualCollection(item.getDepartments(), departments)
                            && CollectionUtils.isEqualCollection(item.getItems(), items)) {

                        response.put("status", false);
                        response.put("message", alertsRequest.getEmail() + " is already subscribed to a similar Alert");
                        return response;
                    }

                    if (purchaseReq != null && item.getPurchaseReq() != null
                            && purchaseReq.equals(item.getPurchaseReq())) {
                        response.put("status", false);
                        response.put("message", alertsRequest.getEmail() + " is already subscribed to a similar Alert");
                        return response;
                    }
                }
            }

            final Alert alert = new Alert(alertsRequest.getEmail(), departments, items, purchaseReq);
            final String secret = RandomStringUtils.randomAlphanumeric(32);
            alert.setSecret(secret);
            alert.setSecretValidUntil(LocalDateTime.now().plusDays(1));

            // save alert and send email verification message.
            alertService.saveAndFlush(alert);
            alertsEmailService.sendVerifyEmail(alert);

            // clear "servicesCache" cache;
            final CacheManager cm = CacheManager.getInstance();
            final Cache servicesCache = cm.getCache("servicesCache");
            if (servicesCache != null) {
                servicesCache.removeAll();
            }

            response.put("status", true);
            return response;
        } catch (Exception e) {
            logger.error("Exception while subscribing to alerts", e);
            throw e;
        }
    }

    @Operation(summary = "Unsubscribe to a particular Email Alert")
    @RequestMapping(value = "/api/makueni/alerts/unsubscribeAlert",
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = "application/json")
    public Boolean unsubscribeAlert() {

        return true;
    }
}
