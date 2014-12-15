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
package io.soabase.core.features.attributes;

import io.soabase.core.listening.Listenable;
import io.soabase.core.rest.entities.Attribute;
import java.util.Collection;

public interface SoaDynamicAttributes extends Iterable<Attribute>
{
    public String getAttribute(String key);

    public String getAttribute(String key, String defaultValue);

    public boolean getAttributeBoolean(String key);

    public boolean getAttributeBoolean(String key, boolean defaultValue);

    public int getAttributeInt(String key);

    public int getAttributeInt(String key, int defaultValue);

    public long getAttributeLong(String key);

    public long getAttributeLong(String key, long defaultValue);

    public void temporaryOverride(String key, boolean value);

    public void temporaryOverride(String key, int value);

    public void temporaryOverride(String key, long value);

    public void temporaryOverride(String key, String value);

    public boolean removeOverride(String key);

    public Collection<String> getKeys();

    public Listenable<SoaDynamicAttributeListener> getListenable();
}
