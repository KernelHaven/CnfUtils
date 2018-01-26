package net.ssehub.kernel_haven.cnf;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.cnf.FormulaToCnfConverterFactory.Strategy;

/**
 * Tests the {@link FormulaToCnfConverterFactory} factory.
 * 
 * @author Adam
 */
public class FormulaToCnfConverterFactoryTest {

    /**
     * Tests the default converter.
     */
    @Test
    public void testDefault() {
        assertThat(FormulaToCnfConverterFactory.create(), instanceOf(RecursiveCnfConverter.class));
    }
    
    /**
     * Tests the user defined converters.
     */
    @Test
    public void testDefined() {
        assertThat(FormulaToCnfConverterFactory.create(Strategy.RECURISVE), instanceOf(RecursiveCnfConverter.class));
        assertThat(FormulaToCnfConverterFactory.create(Strategy.RECURISVE_REPLACING),
                instanceOf(RecursiveReplacingCnfConverter.class));
    }
    
}
