package com.wfsample.common;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;

import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.Reporter;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.common.clients.WavefrontClientFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public final class Tracing {
  private Tracing() {
  }

  public static Tracer init(String service) {
    // TODO: Replace this with Wavefront Tracer
		 WavefrontClientFactory wavefrontClientFactory = new WavefrontClientFactory();
     wavefrontClientFactory.addClient("http://localhost:2878/");

     WavefrontSender wavefrontSender = wavefrontClientFactory.getClient();
		 
     String applicationName = "beachshirts-nyc";
     ApplicationTags applicationTags = new ApplicationTags.Builder(applicationName, service).build();
		 
     Reporter wfSpanReporter = new WavefrontSpanReporter.Builder()
			.build(wavefrontSender);
		 
     WavefrontTracer.Builder wfTracerBuilder = new WavefrontTracer.
       Builder(wfSpanReporter, applicationTags);
						 
     wfTracerBuilder.redMetricsCustomTagKeys(new HashSet<String>(Arrays.asList("env")));
		 
     return wfTracerBuilder.build();
		 
    //return NoopTracerFactory.create();
  }

  public static Span startServerSpan(Tracer tracer, javax.ws.rs.core.HttpHeaders httpHeaders, String operationName) {
    // format the headers for extraction
    MultivaluedMap<String, String> rawHeaders = httpHeaders.getRequestHeaders();
    final HashMap<String, String> headers = new HashMap<>();
    for (String key : rawHeaders.keySet()) {
      headers.put(key, rawHeaders.get(key).get(0));
    }

    Tracer.SpanBuilder spanBuilder;
    try {
      SpanContext parentSpanCtx = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
      if (parentSpanCtx == null) {
        spanBuilder = tracer.buildSpan(operationName);
      } else {
        spanBuilder = tracer.buildSpan(operationName).asChildOf(parentSpanCtx);
      }
    } catch (IllegalArgumentException e) {
      spanBuilder = tracer.buildSpan(operationName);
    }
    return spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
  }
}
