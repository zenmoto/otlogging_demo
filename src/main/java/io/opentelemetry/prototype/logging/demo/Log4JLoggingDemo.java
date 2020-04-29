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

import io.opentelemetry.prototype.logging.Log4jAppender;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4JLoggingDemo {
    private static final Logger LOG = LogManager.getLogger(Log4jAppender.class);
    private static final Tracer TRACER =
            OpenTelemetry.getTracerProvider().get("log4jdemo");

    public void doLog() {
        // Set up a new trace
        Span span = TRACER.spanBuilder("log4j_no_exception").startSpan();
        try {
            try (Scope scope = TRACER.withSpan(span)) {
                LOG.info("A happy little tree to go with the happy little clouds");
            }
        } finally {
            span.end();
        }
    }

    public void doExceptionLog() {
        // Set up a new trace
        Span span = TRACER.spanBuilder("log4j_with_exception").startSpan();
        try {
            try (Scope scope = TRACER.withSpan(span)) {
                try {
                    throw new NullPointerException("Oh no, not again!");
                } catch(Throwable t) {
                    LOG.error("round, round, round... GROUND! I wonder if it's friendly?!", t);
                }
            }
        } finally {
            span.end();
        }

    }

}
