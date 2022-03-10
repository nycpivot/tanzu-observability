package com.wfsample.common;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.util.Collections;
import java.util.List;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.client.ClientSpanDecorator;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFilter;

/**
 * Utilities for use by the various beachshirts application related services.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public final class BeachShirtsUtils {

  private BeachShirtsUtils() {
  }

  public static <T> T createProxyClient(String url, Class<T> clazz, Tracer tracer) {
    HttpClient httpClient = HttpClientBuilder.create().setMaxConnTotal(2000).
        setMaxConnPerRoute(1000).build();
    ApacheHttpClient4Engine apacheHttpClient4Engine = new ApacheHttpClient4Engine(httpClient, true);
    ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
    factory.registerProvider(ResteasyJackson2Provider.class);

    ResteasyClientBuilder resteasyClientBuilder = new ResteasyClientBuilder().
        httpEngine(apacheHttpClient4Engine).providerFactory(factory);
    List<ClientSpanDecorator> decoratorList = Collections.singletonList(ClientSpanDecorator.STANDARD_TAGS);
    ClientTracingFilter filter = new ClientTracingFilter(tracer, decoratorList);
    resteasyClientBuilder.register(filter);

    ResteasyWebTarget target = resteasyClientBuilder.build().target(url);
    return target.proxy(clazz);
  }
}
