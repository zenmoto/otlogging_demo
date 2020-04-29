/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.prototype.logging;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.logging.LogChannel;
import io.opentelemetry.logging.LogEntry;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.TracingContextUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "OpenTelemetry", category= Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE)
public class Log4jAppender extends AbstractAppender {
    private LogChannel logChannel;

    protected Log4jAppender(String name, Filter filter) {
        super(name, filter, null, false, new Property[]{});
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void start() {
        super.start();
        logChannel = OpenTelemetry.getLogChannelProvider().getLogChannel();
    }

    @Override
    public void append(LogEvent event) {
        LogEntry entry = new LogEntry();
        entry.setTime(event.getInstant().getEpochMillisecond());
        entry.setMessage(event.getMessage().getFormattedMessage());
        entry.setSeverityNumber(decodeLevel(event.getLevel()));
        entry.setAttribute("log.source", "log4j");
        entry.setSeverityText(event.getLevel().name());

        // TODO: probably should be a set of enums for predefined attribute keys
        entry.setAttribute("logger.name", event.getLoggerName());

        // TODO: throwables
//        Throwable t = event.getMessage().getThrowable();
//        if (t != null) {
//            Map<String, String> exceptionData = new HashMap();
//            exceptionData.put("message", t.getMessage());
//            exceptionData.put("stacktrace", ExceptionSerializer.getStringStacktrace(t));
//            eventData.put("exception", exceptionData);
//        }

        Span span = TracingContextUtils.getCurrentSpan();
        if (span.isRecording()) {
            SpanContext context = span.getContext();
            entry.setSpanId(context.getSpanId().toLowerBase16());
            entry.setTraceId(context.getTraceId().toLowerBase16());
        }
        logChannel.send(entry);
    }

    private LogEntry.Level decodeLevel(Level level) {
        if (level.compareTo(Level.FATAL) >= 0) {
            return LogEntry.Level.FATAL;
        }
        if (level.compareTo(Level.ERROR) > 0) {
            return LogEntry.Level.ERROR;
        }
        if (level.compareTo(Level.WARN) > 0) {
            return LogEntry.Level.WARN;
        }
        if (level.compareTo(Level.INFO) > 0) {
            return LogEntry.Level.INFO;
        }
        if (level.compareTo(Level.DEBUG) > 0) {
            return LogEntry.Level.DEBUG;
        }
        if (level.intLevel() > 0) {
            return LogEntry.Level.TRACE;
        }
        return LogEntry.Level.UNSET;
    }

    @PluginFactory
    public static Log4jAppender createAppender(
            @PluginAttribute("name") final String name,
            @PluginElement("Filter") final Filter filter
    ) {
        return new Log4jAppender(name, filter);
    }
}
