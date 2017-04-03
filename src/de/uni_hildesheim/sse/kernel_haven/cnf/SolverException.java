package de.uni_hildesheim.sse.kernel_haven.cnf;

/**
 * An exception thrown if the SAT solver fails to solve something.
 * 
 * @author Adam
 * @author Johannes
 */
public class SolverException extends Exception {

    private static final long serialVersionUID = 5176824330175347083L;

    /**
     * Creates a new {@link SolverException}.
     */
    public SolverException() {
    }
    
    /**
     * Creates a new {@link SolverException}.
     * 
     * @param cause The cause that caused this exception.
     */
    public SolverException(Throwable cause) {
        super(cause);
    }
    
}
