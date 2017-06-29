package net.ssehub.kernel_haven.cnf;

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
    public ConverterException(String message) {
        super(message);
    }
    
    /**
     * Creates a new {@link ConverterException} with the given cause.
     * 
     * @param cause The cause of this exception.
     */
    public ConverterException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Creates a new {@link ConverterException} with the given message and cause.
     * 
     * @param message The message to display.
     * @param cause The cause of this exception.
     */
    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
