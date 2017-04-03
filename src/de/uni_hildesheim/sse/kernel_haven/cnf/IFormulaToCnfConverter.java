package de.uni_hildesheim.sse.kernel_haven.cnf;

import de.uni_hildesheim.sse.kernel_haven.util.logic.Formula;

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
    public Cnf convert(Formula formula) throws ConverterException;

}
