package net.ssehub.kernel_haven.cnf;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link Sat4jSolver} class.
 *
 * @author Adam
 */
public class SatSolverTest extends AbstractSatSolverTest {

    @Override
    protected @NonNull ISatSolver createSatSolver() {
        return new Sat4jSolver();
    }

    @Override
    protected @NonNull ISatSolver createSatSolver(@NonNull Cnf cnf) {
        return new Sat4jSolver(cnf);
    }

}
