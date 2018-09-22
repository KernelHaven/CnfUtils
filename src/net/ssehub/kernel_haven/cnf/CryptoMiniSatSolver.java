package net.ssehub.kernel_haven.cnf;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * SAT solver based on <a href="https://github.com/msoos/cryptominisat">CryptoMiniSat</a>. This currently runs only on
 * Linux 64 bit, since it requires JNI bindings.
 * 
 * @author Adam
 */
class CryptoMiniSatSolver extends AbstractSingleShotSatSolver {

    /**
     * Whether we have loaded the native library yet.
     */
    private static boolean initiailized = false;
    
    private @Nullable IntBuffer directBuffer;
    
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
     * @param numVars The number of the highest variable used. Must be > 0.
     * @param numClauses The number of clauses in the buffer. Must be > 0.
     * @param intBuffer A flat buffer of all the CNF clauses to add. The first element is the length of the first
     *      clause, followed by this many clause elements. After this comes the length of the second clause, etc. Clause
     *      elements are integers representing variables, while negative values are negated (just like CNF). 
     * 
     * @return Whether the CNF is satisfiable or not.
     * 
     * @throws SolverException If JNI communication fails or the CMS solver throws an exception.
     */
    private static native boolean isSatisfiableImpl(int numVars, int numClauses, @NonNull IntBuffer intBuffer)
            throws SolverException;
    
    /**
     * Returns a direct {@link IntBuffer} with at least the specified capacity. If applicable, old buffers are re-used
     * so that we don't have to allocate new buffers all the time.
     * 
     * @param requiredCapacity The number of ints that need to be stored.
     * 
     * @return An {@link IntBuffer} with at least the specified capacity.
     * 
     * @throws SolverException If no direct buffer can be created.
     */
    private @NonNull IntBuffer getDirectBuffer(int requiredCapacity) throws SolverException {
        IntBuffer result;
        
        IntBuffer previousBuffer = this.directBuffer;
        if (previousBuffer != null && previousBuffer.capacity() >= requiredCapacity) {
            // re-use old buffer, since its large enough
            previousBuffer.clear(); // make sure it's empty
            result = previousBuffer;
            
        } else {
            result = notNull(
                    ByteBuffer.allocateDirect(requiredCapacity * 4).order(ByteOrder.nativeOrder()).asIntBuffer());
            
            if (!result.isDirect()) {
                throw new SolverException("Unable to create direct IntBuffer");
            }
        }
        
        // store for future use
        this.directBuffer = result;
        
        return result;
    }
    
    @Override
    protected boolean isSatisfiable(int numVars, int[][] clauses) throws SolverException {
        // store the clauses as a flat list in an IntBuffer, so that JNI can directly access the memory
        
        // first calculate the capacity: for each row the number of elements + 1 for the size
        int capacity = 0;
        for (int[] clause : clauses) {
            capacity += clause.length + 1;
        }
        
        IntBuffer buffer = getDirectBuffer(capacity);
        
        for (int[] clause : clauses) {
            buffer.put(clause.length);
            buffer.put(clause);
        }
        
        return isSatisfiableImpl(numVars, clauses.length, buffer);
    }

}
