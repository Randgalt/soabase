package io.soabase.core.features.discovery;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.setup.Environment;
import io.soabase.core.SoaConfiguration;
import java.util.Collection;

@JsonTypeName("default")
public class NullDiscoveryFactory implements SoaDiscoveryFactory
{
    @Override
    public SoaDiscovery build(int mainPort, SoaConfiguration configuration, Environment environment)
    {
        return new SoaDiscovery()
        {
            @Override
            public Collection<String> getCurrentServiceNames()
            {
                return ImmutableSet.of();
            }

            @Override
            public HealthyState getHealthyState()
            {
                return HealthyState.HEALTHY;
            }

            @Override
            public ForcedState getForcedState()
            {
                return ForcedState.CLEARED;
            }

            @Override
            public Collection<SoaDiscoveryInstance> getAllInstances(String serviceName)
            {
                return ImmutableSet.of();
            }

            @Override
            public SoaDiscoveryInstance getInstance(String serviceName)
            {
                return null;
            }

            @Override
            public void noteError(String serviceName, SoaDiscoveryInstance errorInstance)
            {
                // NOP
            }

            @Override
            public void setHealthyState(HealthyState healthyState)
            {
                // NOP
            }

            @Override
            public void setForcedState(ForcedState forcedState)
            {
                // NOP
            }
        };
    }
}
