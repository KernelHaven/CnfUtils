package net.ssehub.kernel_haven.cnf;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * SAT solver based on <a href="https://www.sat4j.org/">Sat4j</a>.
 * 
 * @author Adam
 */
class Sat4jSolver extends AbstractSingleShotSatSolver {
    
    /**
     * Creates a new and empty Sat solver.
     */
    public Sat4jSolver() {
    }
    
    
    /**
     * Creates a SAT solver with the given CNF. This CNF will be used as a basis for each successive call
     * to isSatisfiable(). This version is more performant if the same CNF is checked against a lot
     * of other CNFs.
     * 
     * @param cnf The base CNF.
     */
    public Sat4jSolver(@NonNull Cnf cnf) {
        super(cnf);
    }
    
    /**
     * Creates a new solver.
     * 
     * @return The solver that can be used.
     */
    private @NonNull ISolver createSolver() {
        ISolver solver = SolverFactory.newDefault();
        solver.setDBSimplificationAllowed(false);
        
        return solver;
    }


    @Override
    protected boolean isSatisfiable(int numVars, int[][] clauses) throws SolverException {
        boolean sat = false;
        
        try {
            ISolver solver = createSolver();

            for (int[] clause : clauses) {
                solver.addClause(new VecInt(clause));
            }
            
            try {
                sat = solver.isSatisfiable();
            } catch (TimeoutException e) {
                throw new SolverException(e);
            }
        } catch (ContradictionException e) {
            // sat is already false
        }
        
        return sat;
    }
    
    
}
