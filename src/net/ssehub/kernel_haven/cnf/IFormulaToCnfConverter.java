package net.ssehub.kernel_haven.cnf;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A converter to convert boolean {@link Formula}s to {@link Cnf}.
 * 
 * @author Adam
 * @author Johannes
 */
public interface IFormulaToCnfConverter {
    
    /**
     * Converts the given boolean formula to CNF.
     * 
     * @param formula The formula to convert. Must not be <code>null</code>.
     * @return The CNF representing the formula. Never <code>null</code>.
     * 
     * @throws ConverterException If the conversion fails.
     */
    public @NonNull Cnf convert(@NonNull Formula formula) throws ConverterException;

}
