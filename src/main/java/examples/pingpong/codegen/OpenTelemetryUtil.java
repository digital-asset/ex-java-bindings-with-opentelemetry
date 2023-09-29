// Copyright (c) 2023 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: 0BSD

package examples.pingpong.codegen;

import com.daml.ledger.api.v1.TraceContextOuterClass.TraceContext;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class OpenTelemetryUtil {

    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    private final GrpcTelemetry grpcTelemetry;

    /**
     * Construct an OpenTelemetryUtil object.
     *
     * It comes preconfigured with a jaeger reporter sending spans to `http://localhost:14250`
     *
     * @param serviceName    name to be used for reporting to the open telemetry server
     */
    public OpenTelemetryUtil(String serviceName) {
        this.openTelemetry = createOpenTelemetry(serviceName);
        this.tracer = openTelemetry.getTracer(serviceName);
        this.grpcTelemetry = GrpcTelemetry.create(openTelemetry);
    }

    /**
     * Create open telemetry fully equipped with a jaeger reporter, a tracer provider and
     * a span propagator. This is a minimum set of objects required to successfully create,
     * propagate and report spans to a jaeger server.
     * @return      an OpenTelemetry object
     */
    private static OpenTelemetry createOpenTelemetry(String serviceName) {
        final var spanExporter = JaegerGrpcSpanExporter
                .builder()
                .setEndpoint("http://localhost:14250")
                .setTimeout(30, TimeUnit.SECONDS)
                .build();
        final var batchProcessor = BatchSpanProcessor
                .builder(spanExporter) // change batch size and delay if needed
                .build();
        final var attributes = Attributes
                .builder()
                .put(ResourceAttributes.SERVICE_NAME, serviceName)
                .build();
        final var serviceNameResource = Resource.create(attributes);
        final var tracerProvider = SdkTracerProvider
                .builder()
                .addSpanProcessor(batchProcessor)
                .setSampler(Sampler.alwaysOn())
                .setResource(Resource.getDefault().merge(serviceNameResource))
                .build();
        final var contextPropagators = ContextPropagators.create(W3CTraceContextPropagator.getInstance());
        return OpenTelemetrySdk
                .builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(contextPropagators)
                .build();
    }

    /**
     * Supplement the channel builder with a grpc interceptor capable of inserting ambient spans
     * into the HTTP2 headers of the grpc communication.
     * @param channelBuilder    a channel builder to be supplemented with an interceptor
     * @return                  a supplemented ManagedChannelBuilder.
     * @param <T>               ManagedChannelBuilder's type parameter.
     */
    public <T extends ManagedChannelBuilder<T>> ManagedChannelBuilder<T> withClientInterceptor(ManagedChannelBuilder<T> channelBuilder) {
        return channelBuilder.intercept(grpcTelemetry.newClientInterceptor());
    }

    /**
     * Convert a source trace context in the ledger api format into an open telemetry context.
     * It can then be used for constructing spans that will be children of the source trace context.
     * @param traceContext  source trace context in the ledger api format.
     * @return              open telemetry Context
     */
    public Context contextFromDamlTraceContext(TraceContext traceContext) {
        TextMapGetter<TraceContext> getter = new TextMapGetter<>() {
            @Override
            @Nullable
            public String get(@Nullable TraceContext carrier, String key) {
                return carrier == null ? null : toMap(carrier).get(key);
            }
            @Override
            public Iterable<String> keys(TraceContext carrier) {
                return toMap(carrier).keySet();
            }

            private final String TRACEPARENT_HEADER_NAME = "traceparent"; // same as W3CTraceContextPropagator.TRACE_PARENT
            private final String TRACESTATE_HEADER_NAME = "tracestate";   // same as W3CTraceContextPropagator.TRACE_STATE

            private Map<String, String> toMap(TraceContext traceContext) {
                return Map.of(
                        TRACEPARENT_HEADER_NAME,
                        traceContext.getTraceparent().getValue(),
                        TRACESTATE_HEADER_NAME,
                        traceContext.getTracestate().getValue()
                );
            }
        };
        return openTelemetry
                .getPropagators()
                .getTextMapPropagator()
                .extract(Context.root(), traceContext, getter);
    }

    /**
     * Open a new scope by making the supplied open telemetry context the current one.
     * Run the supplied lambda in that scope. Close the scope afterwards.
     * @param context   open telemetry context to be used.
     * @param body      a function to be run within the open telemetry scope.
     * @return          a value returned by the body function.
     * @param <R>       the type returned by the body function.
     */
    public <R> R runInOpenTelemetryScope(Context context, Supplier<R> body) {
        try (Scope ignored = context.makeCurrent()) {
            return body.get();
        }
    }

    /**
     * Start a new span, run the supplied function in that span, then close it.
     * @param spanName      the name of the span that will appear in the jaeger report.
     * @param body          a function to be run within the open telemetry span.
     * @return              a value returned by the body function.
     * @param <R>           he type returned by the body function.
     */
    public <R> R runInNewSpan(String spanName, Supplier<R> body) {
        Span span = tracer.spanBuilder(spanName).startSpan();
        try(Scope ignored = span.makeCurrent()) {
            return body.get();
        } finally {
            span.end();
        }
    }
}
