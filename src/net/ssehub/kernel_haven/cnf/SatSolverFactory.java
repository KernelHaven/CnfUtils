package net.ssehub.kernel_haven.cnf;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.EnumSetting;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * A factory for creating {@link ISatSolver}s.
 *
 * @author Adam
 */
public class SatSolverFactory {

    public static final @NonNull EnumSetting<@NonNull SolverType> SOLVER_SETTING
        = new EnumSetting<>("cnf.solver", SolverType.class, true, SolverType.SAT4J, "Defines which SAT solver to use.");
    
    /**
     * Enumeration of all supported Sat solvers.
     */
    public enum SolverType {
        
        /**
         * A solver using <a href="https://www.sat4j.org/">Sat4j</a>. This is a pure Java implementation, and thus
         * platform independent. However, it is not so fast.
         */
        SAT4J,
        
        /**
         * A solver using <a href="https://github.com/msoos/cryptominisat">CryptoMiniSat</a>. This uses JNI to use the
         * C++ sat solver; currently only Linux 64 bit is supported. This solver is probalby a lot faster than Sat4j.
         */
        CRYPTOMINISAT,
    }
    
    /**
     * Creates a default SAT solver. Currently, this is Sat4j. This is equal to createSolver(null, null, null, false).
     * 
     * @return The solver.
     */
    public static @NonNull ISatSolver createDefaultSolver() {
        try {
            return createSolver(null, null, null, false);
        } catch (SetUpException e) {
            // can't happen.
            throw new RuntimeException(e);
        }
    }
    
    /**
     * <p>
     * Creates a SAT solver with the given configuration or parameter. The type parameter takes precedence over the
     * configuration. All parameters are optional (all may be <code>null</code>).
     * </p>
     * <p>
     * Optionally a base CNF can be passed to this call. This CNF will be used as a basis for each successive call
     * to isSatisfiable(). This version is more performant if the same CNF is checked against a lot
     * of other CNFs.
     * </p>
     * 
     * @param config The configuration to read the solver type from. If this is <code>null</code>, then a Sat4j solver
     *      will be created.
     * @param type If not <code>null</code>, then a solver of this type will be created (overwrites the config).
     * @param cnf The base CNF. Leave this as <code>null</code> if no base CNF is wanted.
     * @param cached Whether to wrap a cache around this solver. If unsure, say <code>false</code> here.
     * 
     * @return An instance of the given solver.
     * 
     * @throws SetUpException If creating the solver fails.
     */
    public static @NonNull ISatSolver createSolver(@Nullable Configuration config, @Nullable SolverType type,
            @Nullable Cnf cnf, boolean cached)
            throws SetUpException {
        
        if (type == null) {
            if (config != null) {
                config.registerSetting(SOLVER_SETTING);
                type = config.getValue(SOLVER_SETTING);
            } else {
                type = SolverType.SAT4J;
            }
        }
        
        ISatSolver result;
        
        switch (type) {
        
        case SAT4J:
            if (cnf != null) {
                result = new Sat4jSolver(cnf);
            } else {
                result = new Sat4jSolver();
            }
            break;
            
        case CRYPTOMINISAT:
            if (cnf != null) {
                result = new CryptoMiniSatSolver(cnf);
            } else {
                result = new CryptoMiniSatSolver();
            }
            break;
            
        default:
            throw new SetUpException("Unsupported type of solver: " + type);
        }
        
        if (cached) {
            result = new CachedSatSolver(result);
        }
        
        return result;
    }
    
}
