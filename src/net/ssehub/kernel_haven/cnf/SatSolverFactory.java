/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ssehub.kernel_haven.cnf;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.EnumSetting;
import net.ssehub.kernel_haven.util.Logger;
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
    
    private static @NonNull SolverType configuredType = SolverType.SAT4J;
    
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
     * Initialization method called by KernelHaven. See loadClasses.txt
     * 
     * @param config The global pipeline configuration.
     * 
     * @throws SetUpException If the configuration is not valid.
     */
    public static void initialize(@NonNull Configuration config) throws SetUpException {
        config.registerSetting(SOLVER_SETTING);
        configuredType = config.getValue(SOLVER_SETTING);

        Logger.get().logDebug2("Creating SAT solvers of type ", configuredType);
    }
    
    /**
     * Creates a SAT solver as specified in the configuration. By default, is Sat4j (unless configured otherwise).
     * 
     * @return The solver.
     */
    public static @NonNull ISatSolver createSolver() {
        return createSolver(configuredType, null, false);
    }
    
    /**
     * <p>
     * Creates a SAT solver as specified in the configuration. By default, is Sat4j (unless configured otherwise).
     * </p>
     * <p>
     * Optionally a base CNF can be passed to this call. This CNF will be used as a basis for each successive call
     * to isSatisfiable(). This version is more performant if the same CNF is checked against a lot
     * of other CNFs.
     * </p>
     * 
     * @param cnf The base CNF. Leave this as <code>null</code> if no base CNF is wanted.
     * @param cached Whether to wrap a cache around this solver. If unsure, say <code>false</code> here.
     * 
     * @return The solver.
     */
    public static @NonNull ISatSolver createSolver(@Nullable Cnf cnf, boolean cached) {
        return createSolver(configuredType, cnf, cached);
    }
    
    /**
     * Creates a SAT solver instance with the given type.
     * 
     * @param type The type of solver to create.
     * 
     * @return An instance of the given solver.
     */
    public static @NonNull ISatSolver createSolver(@NonNull SolverType type) {
        return createSolver(type, null, false);
    }
    
    /**
     * <p>
     * Creates a SAT solver instance with the given type.
     * </p>
     * <p>
     * Optionally a base CNF can be passed to this call. This CNF will be used as a basis for each successive call
     * to isSatisfiable(). This version is more performant if the same CNF is checked against a lot
     * of other CNFs.
     * </p>
     * 
     * @param type The type of solver to create.
     * @param cnf The base CNF. Leave this as <code>null</code> if no base CNF is wanted.
     * @param cached Whether to wrap a cache around this solver. If unsure, say <code>false</code> here.
     * 
     * @return An instance of the given solver.
     */
    public static @NonNull ISatSolver createSolver(@NonNull SolverType type, @Nullable Cnf cnf, boolean cached) {
        
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
            // shouldn't happen
            throw new RuntimeException("Unsupported type of solver: " + type);
        }
        
        if (cached) {
            result = new CachedSatSolver(result);
        }
        
        return result;
    }
    
}
