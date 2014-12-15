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
package io.soabase.zookeeper.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.soabase.core.SoaFeatures;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CuratorConfiguration
{
    @Valid
    @NotNull
    private String connectionString;

    @Valid
    @NotNull
    private String curatorName = SoaFeatures.DEFAULT_NAME;

    @JsonProperty("connectionString")
    public String getConnectionString()
    {
        return connectionString;
    }

    @JsonProperty("connectionString")
    public void setConnectionString(String connectionString)
    {
        this.connectionString = connectionString;
    }


    @JsonProperty("name")
    public String getCuratorName()
    {
        return curatorName;
    }

    @JsonProperty("name")
    public void setCuratorName(String curatorName)
    {
        this.curatorName = curatorName;
    }
}
