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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.logging.LogEntry;
import io.opentelemetry.logging.LogChannel;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.TracingContextUtils;

public class LogbackAppender<E> extends AppenderBase<E> {
    LogChannel logChannel;

    @Override
    public void start() {
        super.start();
        logChannel = OpenTelemetry.getLogChannelProvider().getLogChannel();
    }

    @Override
    public void stop() {
        super.stop();
        // TODO: Figure out lifecycle
    }

    @Override
    protected void append(E eventObject) {
        if (eventObject instanceof LoggingEvent) {
            LogEntry logEntry = new LogEntry();
            LoggingEvent event = (LoggingEvent) eventObject;
            logEntry.setTime(event.getTimeStamp());
            logEntry.setSeverityNumber(decodeSeverity(event.getLevel()));
            logEntry.setSeverityText(event.getLevel().toString());
            logEntry.setAttribute("logger.name", event.getLoggerName());
            logEntry.setAttribute("log.source", "logback");
            logEntry.setMessage(event.getMessage());
            Span span = TracingContextUtils.getCurrentSpan();
            if (span.isRecording()) {
                SpanContext context = span.getContext();
                logEntry.setSpanId(context.getSpanId().toLowerBase16());
                logEntry.setTraceId(context.getTraceId().toLowerBase16());
            }

            // TODO: Add exception data
//            IThrowableProxy throwableProxy = event.getThrowableProxy();
//            if (throwableProxy != null) {
//                Map<String, String> exceptionData = new HashMap<>(2);
//                exceptionData.put("message", throwableProxy.getMessage());
//                exceptionData.put("stacktrace", ThrowableProxyUtil.asString(throwableProxy));
//                eventData.put("exception", exceptionData);
//            }
            logChannel.send(logEntry);
        }
    }

    private LogEntry.Level decodeSeverity(Level level) {
        int logbackLevel =  level.toInt();
        if (logbackLevel >= Level.ERROR_INT) {
            return LogEntry.Level.ERROR;
        }
        if (logbackLevel >= Level.WARN_INT) {
            return LogEntry.Level.WARN;
        }
        if (logbackLevel >= Level.INFO_INT) {
            return LogEntry.Level.INFO;
        }
        if (logbackLevel >= Level.DEBUG_INT) {
            return LogEntry.Level.DEBUG;
        }
        if (logbackLevel > 0) {
            return LogEntry.Level.TRACE;
        }
        return LogEntry.Level.UNSET;
    }
}
