package com.github.thundax.bacon.inventory.application.support;

import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class InventoryTransactionExecutor {

    private final TransactionTemplate transactionTemplate;

    public InventoryTransactionExecutor() {
        this.transactionTemplate = null;
    }

    public InventoryTransactionExecutor(ObjectProvider<PlatformTransactionManager> transactionManagerProvider) {
        PlatformTransactionManager transactionManager = transactionManagerProvider.getIfAvailable();
        if (transactionManager == null) {
            this.transactionTemplate = null;
            return;
        }
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate = template;
    }

    public <T> T executeInNewTransaction(Supplier<T> action) {
        if (transactionTemplate == null) {
            return action.get();
        }
        return transactionTemplate.execute(status -> action.get());
    }
}
