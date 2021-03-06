/**
 * Copyright 2014 Jordan Zimmerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.soabase.client.apache;

import io.soabase.core.features.client.ClientUtils;
import io.soabase.core.features.client.RetryComponents;
import io.soabase.core.features.client.RetryContext;
import io.soabase.core.features.discovery.SoaDiscovery;
import io.soabase.core.features.discovery.SoaDiscoveryInstance;
import io.soabase.core.features.client.SoaRequestId;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("deprecation")
public class WrappedHttpClient implements HttpClient
{
    private final HttpClient implementation;
    private final SoaDiscovery discovery;
    private final RetryComponents retryComponents;

    public WrappedHttpClient(HttpClient implementation, SoaDiscovery discovery, RetryComponents retryComponents)
    {
        this.implementation = implementation;
        this.discovery = discovery;
        this.retryComponents = retryComponents;
    }

    @Override
    public HttpParams getParams()
    {
        return implementation.getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager()
    {
        return implementation.getConnectionManager();
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws IOException
    {
        return execute(request, (HttpContext)null);
    }

    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException
    {
        addRequestId(request);

        RetryContext retryContext = new RetryContext(retryComponents, request.getURI(), request.getMethod());
        for ( int retryCount = 0; /* no check */; ++retryCount )
        {
            SoaDiscoveryInstance instance = ClientUtils.hostToInstance(discovery, retryContext.getOriginalHost());
            retryContext.setInstance(instance);

            URI filteredUri = ClientUtils.filterUri(request.getURI(), instance);
            if ( filteredUri != null )
            {
                request = new WrappedHttpUriRequest(request, filteredUri);
            }
            try
            {
                HttpResponse response = implementation.execute(request, context);
                if ( !retryContext.shouldBeRetried(retryCount, response.getStatusLine().getStatusCode(), null) )
                {
                    return response;
                }
            }
            catch ( IOException e )
            {
                if ( !retryContext.shouldBeRetried(retryCount, 0, e) )
                {
                    throw e;
                }
            }
        }
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException
    {
        return execute(target, request, (HttpContext)null);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException
    {
        addRequestId(request);

        URI uri;
        try
        {
            uri = new URI(request.getRequestLine().getUri());
        }
        catch ( URISyntaxException e )
        {
            // TODO logging
            throw new IOException(e);
        }

        RetryContext retryContext = new RetryContext(retryComponents, uri, request.getRequestLine().getMethod());
        for ( int retryCount = 0; /* no check */; ++retryCount )
        {
            try
            {
                SoaDiscoveryInstance instance = ClientUtils.hostToInstance(discovery, retryContext.getOriginalHost());
                retryContext.setInstance(instance);

                URI filteredUri = ClientUtils.filterUri(uri, instance);
                if ( filteredUri != null )
                {
                    target = toHost(filteredUri);
                    request = new WrappedHttpRequest(request, filteredUri);
                }
                HttpResponse response = implementation.execute(target, request, context);
                if ( !retryContext.shouldBeRetried(retryCount, response.getStatusLine().getStatusCode(), null) )
                {
                    return response;
                }
            }
            catch ( IOException e )
            {
                if ( !retryContext.shouldBeRetried(retryCount, 0, e) )
                {
                    throw e;
                }
            }
        }
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException
    {
        return internalExecute(request, null, null, responseHandler, null);
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException
    {
        return internalExecute(request, null, null, responseHandler, context);
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException
    {
        return internalExecute(null, target, request, responseHandler, null);
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException
    {
        return internalExecute(null, target, request, responseHandler, context);
    }

    private void addRequestId(HttpRequest request)
    {
        SoaRequestId.HeaderSetter<HttpRequest> setter = new SoaRequestId.HeaderSetter<HttpRequest>()
        {
            @Override
            public void setHeader(HttpRequest request, String header, String value)
            {
                request.addHeader(header, value);
            }
        };
        SoaRequestId.checkSetHeaders(request, setter);
    }

    private HttpHost toHost(URI uri)
    {
        return new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
    }

    // mostly copied from CloseableHttpClient.execute()
    private <T> T internalExecute(final HttpUriRequest uriRequest, final HttpHost target, final HttpRequest request,
                         final ResponseHandler<? extends T> responseHandler, final HttpContext context)
        throws IOException
    {
        Args.notNull(responseHandler, "Response handler");

        final HttpResponse response = (uriRequest != null) ? execute(uriRequest, context) : execute(target, request, context);

        final T result;
        try {
            result = responseHandler.handleResponse(response);
        } catch (final Exception t) {
            final HttpEntity entity = response.getEntity();
            try {
                EntityUtils.consume(entity);
            } catch (final Exception t2) {
                // Log this exception. The original exception is more
                // important and will be thrown to the caller.
                // TODO this.log.warn("Error consuming content after an exception.", t2);
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            throw new UndeclaredThrowableException(t);
        }

        // Handling the response was successful. Ensure that the content has
        // been fully consumed.
        final HttpEntity entity = response.getEntity();
        EntityUtils.consume(entity);
        return result;
    }
}
