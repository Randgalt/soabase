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
package io.soabase.admin.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.soabase.admin.details.IndexServlet;
import io.soabase.core.SoaBundle;
import io.soabase.core.SoaFeatures;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import java.util.List;
import java.util.prefs.Preferences;

public class ComponentBundle implements Bundle
{
    private final String appName;
    private final String companyName;
    private final String footerMessage;
    private final List<MetricComponent> metrics;
    private final List<TabComponent> tabs;

    public ComponentBundle(String appName, String companyName, String footerMessage, List<TabComponent> tabs, List<MetricComponent> metrics)
    {
        this.appName = appName;
        this.companyName = companyName;
        this.footerMessage = footerMessage;
        this.metrics = metrics;
        this.tabs = ImmutableList.copyOf(tabs);
    }

    @Override
    public void run(Environment environment)
    {
        final ComponentManager componentManager = new ComponentManager(appName, companyName, footerMessage);
        final Preferences preferences = Preferences.userRoot();
        AbstractBinder binder = new AbstractBinder()
        {
            @Override
            protected void configure()
            {
                bind(preferences).to(Preferences.class);
                bind(componentManager).to(ComponentManager.class);
            }
        };
        SoaBundle.getFeatures(environment).putNamed(componentManager, ComponentManager.class, SoaFeatures.DEFAULT_NAME);
        SoaBundle.getFeatures(environment).putNamed(preferences, Preferences.class, SoaFeatures.DEFAULT_NAME);

        componentManager.getTabs().addAll(tabs);
        componentManager.getMetrics().addAll(metrics);

        List<IndexServlet.Mapping> mappings = Lists.newArrayList
        (
            new IndexServlet.Mapping("", "index.html"),
            new IndexServlet.Mapping("/vm", "vm.html"),
            new IndexServlet.Mapping("/logs", "logs.html")
        );
        IndexServlet servlet = new IndexServlet(componentManager, mappings);
        servlet.setServlets(environment.servlets());

        environment.jersey().register(binder);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
        // NOP
    }
}
