package net.ssehub.kernel_haven.cnf;

import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Thrown by converters for boolean formulas if they fail.
 * 
 * @author Adam
 * @author Johannes
 */
public class ConverterException extends Exception {

    private static final long serialVersionUID = -2153432140783061540L;

    /**
     * Creates a new {@link ConverterException} with the given message.
     * 
     * @param message The message to display.
     */
    public ConverterException(@Nullable String message) {
        super(message);
    }
    
    /**
     * Creates a new {@link ConverterException} with the given cause.
     * 
     * @param cause The cause of this exception.
     */
    public ConverterException(@Nullable Throwable cause) {
        super(cause);
    }
    
    /**
     * Creates a new {@link ConverterException} with the given message and cause.
     * 
     * @param message The message to display.
     * @param cause The cause of this exception.
     */
    public ConverterException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
    
}
