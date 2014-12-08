package io.soabase.core;

import com.google.common.collect.Lists;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.soabase.core.features.attributes.SoaDynamicAttributes;
import io.soabase.core.features.discovery.HealthCheckIntegration;
import io.soabase.core.features.discovery.SoaDiscovery;
import io.soabase.core.features.discovery.SoaDiscoveryHealth;
import io.soabase.core.rest.DiscoveryApis;
import io.soabase.core.rest.DynamicAttributeApis;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SoaBundle<T extends Configuration> implements ConfiguredBundle<T>
{
    private final ConfigurationAccessor<T, SoaConfiguration> configurationAccessor;

    public SoaBundle(ConfigurationAccessor<T, SoaConfiguration> configurationAccessor)
    {
        this.configurationAccessor = new CheckedConfigurationAccessor<>(configurationAccessor);
    }

    @Override
    public void run(final T configuration, Environment environment) throws Exception
    {
        final SoaConfiguration soaConfiguration = configurationAccessor.accessConfiguration(configuration);

        environment.jersey().register(DiscoveryApis.class);
        environment.jersey().register(DynamicAttributeApis.class);

        checkCorsFilter(soaConfiguration, environment);

        updateInstanceName(soaConfiguration);
        List<String> scopes = Lists.newArrayList();
        scopes.add(soaConfiguration.getInstanceName());
        scopes.addAll(soaConfiguration.getScopes());

        SoaDiscovery discovery = checkManaged(environment, soaConfiguration.getDiscoveryFactory().build(soaConfiguration, environment));
        SoaDynamicAttributes attributes = checkManaged(environment, soaConfiguration.getAttributesFactory().build(soaConfiguration, environment, scopes));
        soaConfiguration.setDiscovery(discovery);
        soaConfiguration.setAttributes(attributes);

        startDiscoveryHealth(discovery, soaConfiguration, environment);

        AbstractBinder binder = new AbstractBinder()
        {
            @Override
            protected void configure()
            {
                bind(configuration).to(Configuration.class);
                bind(soaConfiguration).to(SoaFeatures.class);
            }
        };
        environment.jersey().register(binder);

        Managed managed = new Managed()
        {
            @Override
            public void start() throws Exception
            {
                soaConfiguration.lock();
            }

            @Override
            public void stop() throws Exception
            {
                // NOP
            }
        };
        environment.lifecycle().manage(managed);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
        // NOP
    }

    private void startDiscoveryHealth(SoaDiscovery discovery, SoaConfiguration soaConfiguration, Environment environment)
    {
        SoaDiscoveryHealth discoveryHealth = checkManaged(environment, soaConfiguration.getDiscoveryHealthFactory().build(soaConfiguration, environment));
        ScheduledExecutorService service = environment.lifecycle().scheduledExecutorService("DiscoveryHealthChecker-%d").build();
        service.scheduleAtFixedRate(new HealthCheckIntegration(environment.healthChecks(), discovery, discoveryHealth), soaConfiguration.getDiscoveryHealthCheckPeriodMs(), soaConfiguration.getDiscoveryHealthCheckPeriodMs(), TimeUnit.MILLISECONDS);
    }

    private void checkCorsFilter(SoaConfiguration configuration, Environment environment)
    {
        if ( configuration.isAddCorsFilter() )
        {
            // from http://jitterted.com/tidbits/2014/09/12/cors-for-dropwizard-0-7-x/

            FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
            filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
            filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
            filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
            filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
            filter.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
            filter.setInitParameter("allowCredentials", "true");
        }
    }

    private void updateInstanceName(SoaConfiguration configuration) throws UnknownHostException
    {
        if ( configuration.getInstanceName() == null )
        {
            configuration.setInstanceName(InetAddress.getLocalHost().getHostName());
        }
    }

    private static <T> T checkManaged(Environment environment, T obj)
    {
        if ( obj instanceof Managed )
        {
            environment.lifecycle().manage((Managed)obj);
        }
        return obj;
    }
}
