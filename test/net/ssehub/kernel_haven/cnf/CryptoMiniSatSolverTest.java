package net.ssehub.kernel_haven.cnf;

import org.junit.runner.RunWith;

import net.ssehub.kernel_haven.test_utils.RunOnlyOnLinux;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link Sat4jSolver} class.
 *
 * @author Adam
 */
@RunWith(RunOnlyOnLinux.class)
public class CryptoMiniSatSolverTest extends AbstractSatSolverTest {

    @Override
    protected @NonNull ISatSolver createSatSolver() {
        return new CryptoMiniSatSolver();
    }

    @Override
    protected @NonNull ISatSolver createSatSolver(@NonNull Cnf cnf) {
        return new CryptoMiniSatSolver(cnf);
    }

}
