package org.arquillian.algeron.pact.provider.core;

import java.lang.annotation.Annotation;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class ContractExposerResourceProvider implements ResourceProvider {
    @Override
    public boolean canProvide(Class<?> aClass) {
        return ContractExposer.class.isAssignableFrom(aClass);
    }

    @Override
    public Object lookup(ArquillianResource arquillianResource, Annotation... annotations) {
        return new ContractExposer();
    }
}
