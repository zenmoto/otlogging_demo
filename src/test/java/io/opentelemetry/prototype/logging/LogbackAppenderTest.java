package io.opentelemetry.prototype.logging;

import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporters.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporters.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;
import io.opentelemetry.trace.TracingContextUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

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
class LogbackAppenderTest {
    @Test
    public void testLogging() throws InterruptedException {
        // Get the tracer
        TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();
        JaegerGrpcSpanExporter.Builder builder = JaegerGrpcSpanExporter.Builder.fromEnv();
        builder.setServiceName("WombatWedge");
        builder.setChannel(ManagedChannelBuilder.forTarget("localhost:14250").usePlaintext().build());
        builder.install(tracerProvider);

// Set to export the traces to a logging stream
//        tracerProvider.addSpanProcessor(
//                SimpleSpansProcessor.newBuilder(
//                        new LoggingSpanExporter()
//                ).build());
        // Get the tracer
        Tracer tracer =
                OpenTelemetry.getTracerProvider().get("logging-test");
        Logger logger = LoggerFactory.getLogger("Test");
        Span span = tracer.spanBuilder("testSpan").startSpan();
        try (Scope ws = tracer.withSpan(span)) {
            Span span2 = TracingContextUtils.getCurrentSpan();
            assertSame(span, span2);
            for (int i = 0; i<=1000; i++) {
                logger.info("what! " + i + " " + Thread.currentThread().getId());
            }
        }
        span.end();
        Thread.sleep(10000);
    }

}
