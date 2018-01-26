package net.ssehub.kernel_haven.cnf;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link SatSolver} class.
 *
 * @author Adam
 */
public class SatSolverTest extends AbstractSatSolverTest {

    @Override
    protected @NonNull SatSolver createSatSolver() {
        return new SatSolver();
    }

    @Override
    protected @NonNull SatSolver createSatSolver(@NonNull Cnf cnf) {
        return new SatSolver(cnf);
    }

}
