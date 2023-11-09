package org.devgateway.toolkit.persistence.mongo.spring;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ReplaceRootOperation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by mpostelnicu on 11-May-17.
 */
public final class MongoUtil {

    private MongoUtil() {

    }

    public static ReplaceRootOperation replaceRootWithId() {
        return Aggregation.replaceRoot().withValueOf("_id");
    }

    public static final int BATCH_SIZE = 10000;

    public static Date getDateFromLocalDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }


    public static <T, ID extends Serializable> void processRepositoryItemsPaginated(MongoRepository<T, ID> repository,
                                                                                    Consumer<? super T> action,
                                                                                    Consumer<String> logMessage
    ) {
        int pageNumber = 0;
        AtomicInteger processedCount = new AtomicInteger(0);
        Page<T> page;
        do {
            page = repository.findAll(PageRequest.of(pageNumber++, BATCH_SIZE));
            page.getContent().forEach(action);
            processedCount.addAndGet(page.getNumberOfElements());
            logMessage.accept("Processed " + processedCount.get() + " items");
        } while (!page.isLast());
    }
}
