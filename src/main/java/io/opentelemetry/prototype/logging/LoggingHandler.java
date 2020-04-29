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
import io.opentelemetry.logging.LogEntry;
import io.opentelemetry.logging.LogChannel;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LoggingHandler extends Handler {
    LogChannel logChannel = OpenTelemetry.getLogChannelProvider().getLogChannel();

    @Override
    public void publish(LogRecord record) {
        LogEntry entry = new LogEntry();
        entry.setTime(record.getMillis());
        entry.setMessage(record.getMessage());
        entry.setSeverityText(record.getLevel().getName());
        entry.setSeverityNumber(decodeSeverity(record.getLevel()));
        entry.setAttribute("logger.name", record.getLoggerName());
        entry.setAttribute("log.source", "javalogging");
        // TODO: Handle exceptions
//        if (record.getThrown() != null) {
//            Throwable thrown = record.getThrown();
//            Map<String, String> exception = new HashMap<>(2);
//            exception.put("message", thrown.getMessage());
//            exception.put("stacktrace", ExceptionSerializer.getStringStacktrace(thrown));
//            logData.put("exception", exception);
//        }
        logChannel.send(entry);

    }

    private LogEntry.Level decodeSeverity(Level level) {
        // TODO: do this mapping
        return LogEntry.Level.UNSET;
    }

    @Override
    public void flush() {
        // TODO: figure out how to push flush back through
    }

    @Override
    public void close() throws SecurityException {

    }
}
