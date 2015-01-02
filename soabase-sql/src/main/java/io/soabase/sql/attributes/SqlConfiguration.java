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
package io.soabase.sql.attributes;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.soabase.core.SoaFeatures;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SqlConfiguration
{
    @Valid
    @NotNull
    private String mybatisConfigUrl;

    @Valid
    @NotNull
    private String name = SoaFeatures.DEFAULT_NAME;

    @JsonProperty("mybatisConfigUrl")
    public String getMybatisConfigUrl()
    {
        return mybatisConfigUrl;
    }

    @JsonProperty("mybatisConfigUrl")
    public void setMybatisConfigUrl(String mybatisConfigUrl)
    {
        this.mybatisConfigUrl = mybatisConfigUrl;
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }
}
