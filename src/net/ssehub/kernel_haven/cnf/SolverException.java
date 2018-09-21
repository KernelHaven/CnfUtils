package net.ssehub.kernel_haven.cnf;

import net.ssehub.kernel_haven.util.null_checks.Nullable;

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
     * @param message The message of this message.
     */
    public SolverException(String message) {
        super(message);
    }
    
    /**
     * Creates a new {@link SolverException}.
     * 
     * @param cause The cause that caused this exception.
     */
    public SolverException(@Nullable Throwable cause) {
        super(cause);
    }
    
}
