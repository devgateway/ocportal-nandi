package org.devgateway.toolkit.persistence.fm.service;

import org.devgateway.toolkit.persistence.fm.entity.DgFeature;

import java.util.Collection;
import java.util.List;

/**
 * @author mpostelnicu
 */
public interface DgFmService {

    Collection<DgFeature> getFeatures();

    List<DgFeature> getFeaturesByPrefix(String featureName);

    DgFeature getFeature(String featureName);

    Boolean hasFeature(String featureName);

    Boolean isFeatureEnabled(String featureName);

    Boolean isFeatureMandatory(String featureName);

    Boolean isFeatureVisible(String featureName);

    Boolean isFmActive();

    int featuresCount();

    /**
     * Creates the {@link DgFeature#getName()} by combining the parent FM name with the current feature name.
     * This is generally used by hierarchical component containers (like Wicket) to compile a hierarchical
     * FM name.
     *
     * @param parentFmName
     * @param featureName
     * @return
     */
    String getParentCombinedFmName(String parentFmName, String featureName);
}
