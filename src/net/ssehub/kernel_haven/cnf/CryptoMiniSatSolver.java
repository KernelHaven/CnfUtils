package net.ssehub.kernel_haven.cnf;

import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * SAT solver based on <a href="https://github.com/msoos/cryptominisat">CryptoMiniSat</a>. This currently runs only on
 * Linux 64 bit, since it requires JNI bindings.
 * 
 * @author Adam
 */
class CryptoMiniSatSolver extends AbstractSingleShotSatSolver {

    private static boolean initiailized = false;
    
    /**
     * Creates a new and empty Sat solver.
     * 
     * @throws SetUpException If loading the JNI library fails.
     */
    public CryptoMiniSatSolver() throws SetUpException {
        init();
    }
    
    
    /**
     * Creates a SAT solver with the given CNF. This CNF will be used as a basis for each successive call
     * to isSatisfiable(). This version is more performant if the same CNF is checked against a lot
     * of other CNFs.
     * 
     * @param cnf The base CNF.
     * 
     * @throws SetUpException If loading the JNI library fails.
     */
    public CryptoMiniSatSolver(@NonNull Cnf cnf) throws SetUpException {
        super(cnf);
        init();
    }
    
    /**
     * Loads the JNI library.
     * 
     * @throws SetUpException If loading fails.
     */
    private static synchronized void init() throws SetUpException {
        if (!initiailized) {
            
            try {
                File library = Util.extractJarResourceToTemporaryFile(
                        "net/ssehub/kernel_haven/cnf/cryptominisat5-jni.so");
                System.load(library.getAbsolutePath());
                
            } catch (IOException | SecurityException | UnsatisfiedLinkError e) {
                throw new SetUpException("Can't load JNI library", e);
            }
            
            initiailized = true;
        }
    }
    
    /**
     * The native implementation that calls the CMS solver.
     * 
     * @param numVars The number of the highest variable used.
     * @param clauses The list (first dimension) of clauses (second dimension).
     * 
     * @return Whether the CNF is satisfiable or not.
     */
    private static native boolean isSatisfiableImpl(int numVars, int[][] clauses);
    
    @Override
    protected boolean isSatisfiable(int numVars, int[][] clauses) throws SolverException {
        return isSatisfiableImpl(numVars, clauses);
    }

}
