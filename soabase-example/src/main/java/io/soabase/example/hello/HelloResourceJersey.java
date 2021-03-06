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
package io.soabase.example.hello;

import io.soabase.core.features.client.ClientUtils;
import io.soabase.core.features.client.SoaRequestId;
import io.soabase.core.SoaFeatures;
import io.soabase.core.SoaInfo;
import io.soabase.example.goodbye.GoodbyeResource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path("/hello")
public class HelloResourceJersey
{
    private final SoaInfo info;
    private final Client client;

    @Inject
    public HelloResourceJersey(SoaFeatures features, @Named(SoaFeatures.DEFAULT_NAME) Client client)
    {
        this.info = features.getSoaInfo();
        this.client = client;
    }

    @GET
    public String getHello(@Context HttpHeaders headers) throws Exception
    {
        String result = "Service Name: " + info.getServiceName()
            + "\nInstance Name: " + info.getInstanceName()
            + "\nRequest Id: " + SoaRequestId.get(headers)
            + "\n"
            ;

        URI uri = UriBuilder.fromResource(GoodbyeResource.class).host(ClientUtils.serviceNameToHost("goodbye")).build();
        String value = client.target(uri).request().get(String.class);
        return result + "\nGoodbye app says: \n\t" + value;
    }
}
