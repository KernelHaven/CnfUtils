package net.ssehub.kernel_haven.cnf;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link RecursiveCnfConverter}.
 * 
 * @author Adam
 */
public class RecursiveCnfConverterTest extends AbstractCnfConverterTest {

    @Override
    protected @NonNull IFormulaToCnfConverter createConverter() {
        return new RecursiveCnfConverter();
    }

}
