package net.ssehub.kernel_haven.cnf;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link RecursiveReplacingCnfConverter}.
 * 
 * @author Adam
 */
public class RecursiveReplacingCnfConverterTest extends AbstractCnfConverterTest {

    @Override
    protected @NonNull IFormulaToCnfConverter createConverter() {
        return new RecursiveReplacingCnfConverter();
    }

}
