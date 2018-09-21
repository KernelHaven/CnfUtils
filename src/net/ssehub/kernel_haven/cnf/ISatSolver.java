package net.ssehub.kernel_haven.cnf;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A solver for the satisfiability problem.
 *
 * @author Adam
 */
public interface ISatSolver {

    /**
     * Checks if the given CNF (conjunctive normal form) is satisfiable.
     * 
     * @param cnf The CNF to check.
     * 
     * @return Whether the CNF is satisfiable or not.
     * 
     * @throws SolverException If solving the CNF fails.
     */
    public boolean isSatisfiable(@NonNull Cnf cnf) throws SolverException;
    
}
