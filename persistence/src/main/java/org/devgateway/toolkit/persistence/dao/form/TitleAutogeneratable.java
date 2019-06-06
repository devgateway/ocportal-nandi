package org.devgateway.toolkit.persistence.dao.form;

import org.springframework.util.ObjectUtils;

import java.util.function.Consumer;

public interface TitleAutogeneratable {

    String getTitle();

    Consumer<String> titleSetter();

    default void autogenerateTitleWhenEmpty() {
        if (ObjectUtils.isEmpty(getTitle())) {
            titleSetter().accept(String.format("%s %d (autogenerated, change this text to the real title)",
                    getClass().getSimpleName(), System.currentTimeMillis()));
        }
    }

}
