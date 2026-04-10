package com.github.thundax.bacon.common.core.context;

import org.springframework.core.task.TaskDecorator;

public class BaconContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        return AsyncTaskWrapper.wrap(runnable);
    }
}
