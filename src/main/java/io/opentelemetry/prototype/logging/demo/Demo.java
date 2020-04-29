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

package io.opentelemetry.prototype.logging.demo;

import io.opentelemetry.prototype.logging.LoggingHandler;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporters.fluent.FluentLoggingExporter;
import io.opentelemetry.exporters.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logging.SdkLogChannelProvider;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogManager;

public class Demo {
    private static final Logger LOG = LoggerFactory.getLogger(Demo.class);
    private static final Tracer TRACER =
            OpenTelemetry.getTracerProvider().get("tracing_demo");

    public static void main(String[] args) throws InterruptedException {
        setupTracing();
        setupLogging();
        setupJavaLogging(); // Can also be done via system parameters
        Log4JLoggingDemo log4j = new Log4JLoggingDemo();
        LogbackLoggingDemo logback = new LogbackLoggingDemo();
        JavaLoggingDemo jLogging = new JavaLoggingDemo();

        Span span = TRACER.spanBuilder("demo").startSpan();
        Scope scope = TRACER.withSpan(span);

        try {
            log4j.doLog();
            logback.doLog();
            jLogging.doLog();
            log4j.doExceptionLog();
            logback.doExceptionLog();
            jLogging.doExceptionLog();
        } finally {
            scope.close();
            span.end();
        }
        LOG.info("Out of the trace (this message should not have trace info attached");
        Thread.sleep(10000);
        shutdown();
    }

    private static void setupTracing() {
        TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();
        JaegerGrpcSpanExporter.Builder builder = JaegerGrpcSpanExporter.Builder.fromEnv();
        builder.install(tracerProvider);
    }

    private static void setupLogging() {
        SdkLogChannelProvider loggingProvider = OpenTelemetrySdk.getSdkLogChannelProvider();
        FluentLoggingExporter.Builder builder = FluentLoggingExporter.Builder.fromEnv();
        builder.install(loggingProvider);

    }

    // This is usually done by setting java properties
    private static void setupJavaLogging() {
        LogManager.getLogManager().reset();
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");

        rootLogger.addHandler(new LoggingHandler());
    }

    private static void shutdown() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();
        tracerProvider.shutdown();
    }
}
