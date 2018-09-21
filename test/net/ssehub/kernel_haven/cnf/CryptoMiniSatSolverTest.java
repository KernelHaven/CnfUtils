package net.ssehub.kernel_haven.cnf;

import static org.junit.Assert.fail;

import org.junit.runner.RunWith;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.test_utils.RunOnlyOnLinux;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link Sat4jSolver} class.
 *
 * @author Adam
 */
@SuppressWarnings("null")
@RunWith(RunOnlyOnLinux.class)
public class CryptoMiniSatSolverTest extends AbstractSatSolverTest {

    @Override
    protected @NonNull ISatSolver createSatSolver() {
        ISatSolver result;
        try {
            result = new CryptoMiniSatSolver();
        } catch (SetUpException e) {
            e.printStackTrace();
            fail(e.getMessage());
            result = null;
        }
        return result;
    }

    @Override
    protected @NonNull ISatSolver createSatSolver(@NonNull Cnf cnf) {
        ISatSolver result;
        try {
            result = new CryptoMiniSatSolver(cnf);
        } catch (SetUpException e) {
            e.printStackTrace();
            fail(e.getMessage());
            result = null;
        }
        return result;
    }

}
