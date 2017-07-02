package net.ssehub.kernel_haven.cnf;

/**
 * Tests the {@link SatSolver} class.
 *
 * @author Adam
 */
public class SatSolverTest extends AbstractSatSolverTest {

    @Override
    protected SatSolver createSatSolver() {
        return new SatSolver();
    }

    @Override
    protected SatSolver createSatSolver(Cnf cnf) {
        return new SatSolver(cnf);
    }

}
