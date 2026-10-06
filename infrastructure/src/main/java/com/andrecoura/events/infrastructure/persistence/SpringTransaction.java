package com.andrecoura.events.infrastructure.persistence;

import com.andrecoura.events.application.port.Transaction;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
class SpringTransaction implements Transaction {

    private final TransactionTemplate template;

    SpringTransaction(PlatformTransactionManager transactionManager) {
        this.template = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T execute(Supplier<T> work) {
        return template.execute(status -> work.get());
    }
}
