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
package io.soabase.client;

import com.google.common.base.Preconditions;
import io.soabase.core.features.discovery.SoaDiscovery;
import io.soabase.core.features.discovery.SoaDiscoveryInstance;
import java.net.URI;
import java.net.URISyntaxException;

public class Common
{
    public static String hostToServiceName(String host)
    {
        host = Preconditions.checkNotNull(host, "Request URI's host cannot be null");
        if ( host.startsWith(SoaClientBundle.HOST_SUBSTITUTION_TOKEN) && (host.length() > SoaClientBundle.HOST_SUBSTITUTION_TOKEN.length()) )
        {
            return host.substring(SoaClientBundle.HOST_SUBSTITUTION_TOKEN.length());
        }
        return null;
    }

    public static SoaDiscoveryInstance hostToInstance(SoaDiscovery discovery, String host)
    {
        String serviceName = hostToServiceName(host);
        if ( serviceName != null )
        {
            return Preconditions.checkNotNull(discovery.getInstance(serviceName), "No instance found for " + serviceName);
        }
        return null;
    }

    public static URI filterUri(URI uri, SoaDiscoveryInstance instance)
    {
        if ( instance != null )
        {
            try
            {
                String scheme = instance.isForceSsl() ? "https" : ((uri.getScheme() != null) ? uri.getScheme() : "http");
                return new URI(scheme, uri.getUserInfo(), instance.getHost(), instance.getPort(), uri.getRawPath(), uri.getRawQuery(), uri.getRawFragment());
            }
            catch ( URISyntaxException e )
            {
                // TODO logging
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private Common()
    {
    }
}
