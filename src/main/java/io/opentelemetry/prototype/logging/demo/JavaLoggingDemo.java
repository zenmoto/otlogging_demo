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

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaLoggingDemo {
    private final Logger LOG = Logger.getLogger(JavaLoggingDemo.class.getName());
    private static final Tracer TRACER =
            OpenTelemetry.getTracerProvider().get("java_logging_demo");

    public void doLog() {
        // Set up a new trace
        Span span = TRACER.spanBuilder("java_logging_no_exception").startSpan();
        try {
            try (Scope scope = TRACER.withSpan(span)) {
                LOG.info("It's like magic!");
            }
        } finally {
            span.end();
        }
    }

    public void doExceptionLog() {
        // Set up a new trace
        Span span = TRACER.spanBuilder("java_logging_with_exception").startSpan();
        try {
            try (Scope scope = TRACER.withSpan(span)) {
                try {
                    throw new NullPointerException("kersplat");
                } catch(Throwable t) {
                    LOG.log(Level.SEVERE, "Sad Day.", t);
                }
            }
        } finally {
            span.end();
        }
    }
}
