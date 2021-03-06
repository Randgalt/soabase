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
package io.soabase.core;

import io.soabase.core.features.ExecutorBuilder;
import io.soabase.core.features.attributes.SoaDynamicAttributes;
import io.soabase.core.features.discovery.SoaDiscovery;
import io.soabase.core.features.logging.LoggingReader;

public interface SoaFeatures
{
    public static final String DEFAULT_NAME = "default";
    public static final String ADMIN_NAME = "soa-admin";

    public <T> T getNamed(Class<T> clazz, String name);

    public <T> T getNamedRequired(Class<T> clazz, String name);

    public <T> void putNamed(T o, Class<T> clazz, String name);

    public SoaDiscovery getDiscovery();

    public SoaDynamicAttributes getAttributes();

    public SoaInfo getSoaInfo();

    public ExecutorBuilder getExecutorBuilder();

    public LoggingReader getLoggingReader();
}
