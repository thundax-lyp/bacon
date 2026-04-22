package com.github.thundax.bacon.common.test.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.LoggerFactory;

public final class ExpectedLogCapture implements AutoCloseable {

    private final Logger logger;
    private final Level originalLevel;
    private final boolean originalAdditive;
    private final List<Appender<ILoggingEvent>> originalAppenders;
    private final ListAppender<ILoggingEvent> appender;

    private ExpectedLogCapture(Class<?> loggerClass) {
        this.logger = (Logger) LoggerFactory.getLogger(loggerClass);
        this.originalLevel = logger.getLevel();
        this.originalAdditive = logger.isAdditive();
        this.originalAppenders = currentAppenders(logger);
        this.appender = new ListAppender<>();
        this.appender.start();
        logger.detachAndStopAllAppenders();
        logger.addAppender(appender);
        logger.setAdditive(false);
        logger.setLevel(Level.TRACE);
    }

    public static ExpectedLogCapture capture(Class<?> loggerClass) {
        return new ExpectedLogCapture(loggerClass);
    }

    public boolean contains(String fragment) {
        return appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains(fragment));
    }

    public List<String> messages() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    @Override
    public void close() {
        logger.detachAppender(appender);
        appender.stop();
        originalAppenders.forEach(logger::addAppender);
        logger.setAdditive(originalAdditive);
        logger.setLevel(originalLevel);
    }

    private static List<Appender<ILoggingEvent>> currentAppenders(Logger logger) {
        List<Appender<ILoggingEvent>> appenders = new ArrayList<>();
        Iterator<Appender<ILoggingEvent>> iterator = logger.iteratorForAppenders();
        while (iterator.hasNext()) {
            appenders.add(iterator.next());
        }
        return appenders;
    }
}
