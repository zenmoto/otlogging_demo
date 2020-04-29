package io.opentelemetry.prototype.logging;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporters.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

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
class Log4jAppenderTest {

    @Test
    public void testLog4jOutput() {
        // Get the tracer
        TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();

// Set to export the traces to a logging stream
        tracerProvider.addSpanProcessor(
                SimpleSpansProcessor.newBuilder(
                        new LoggingSpanExporter()
                ).build());
        // Get the tracer
        Tracer tracer =
                OpenTelemetry.getTracerProvider().get("logging-test");
        Span span = tracer.spanBuilder("testSpan").startSpan();
        Logger logger = LogManager.getLogger(Log4jAppender.class);
        try (Scope ws = tracer.withSpan(span)) {
            logger.info("Woow");
        }
        span.end();
    }

}
