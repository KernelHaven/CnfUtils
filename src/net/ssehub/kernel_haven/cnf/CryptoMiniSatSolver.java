package net.ssehub.kernel_haven.cnf;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

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
     * @param numClauses The number of clauses in the buffer.
     * @param buffer A flat buffer of all the CNF clauses to add. The first element is the length of the first clause,
     *      followed by this many clause elements. After this comes the length of the second clause, etc. Clause
     *      elements are integers representing variables, while negative values are negated (just like CNF). 
     * 
     * @return Whether the CNF is satisfiable or not.
     */
    private static native boolean isSatisfiableImpl(int numVars, int numClauses, IntBuffer buffer);
    
    @Override
    protected boolean isSatisfiable(int numVars, int[][] clauses) throws SolverException {
        // store the clauses as a flat list in an IntBuffer, so that JNI can directly access the memory
        
        // first calculate the capacity: for each row the number of elements + 1 for the size
        int capacity = 0;
        for (int[] clause : clauses) {
            capacity += clause.length + 1;
        }
        
        IntBuffer buffer = ByteBuffer.allocateDirect(capacity * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        
        if (!buffer.isDirect()) {
            throw new SolverException("Unable to create direct IntBuffer");
        }
        
        for (int[] clause : clauses) {
            buffer.put(clause.length);
            buffer.put(clause);
        }
        
        return isSatisfiableImpl(numVars, clauses.length, buffer);
    }

}
