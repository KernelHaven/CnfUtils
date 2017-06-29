package net.ssehub.kernel_haven.cnf;

/**
 * Factory for creating FormulaToCnfConverters.
 * 
 * @author Adam
 * @author Johannes
 */
public class FormulaToCnfConverterFactory {

    /**
     * The strategies possible to convert formulas to CNF.
     */
    public enum Strategy {
        
        /**
         * Recursively walk through the formula to convert while transforming it into CNF.
         * See https://www.cs.jhu.edu/~jason/tutorials/convert-to-CNF.html
         */
        RECURISVE,
        
        /**
         * Same as {@linkplain #RECURISVE}, but introduce "switching" variables to keep the 
         * resulting CNF as small as possible.
         * See https://www.cs.jhu.edu/~jason/tutorials/convert-to-CNF.html
         */
        RECURISVE_REPLACING,
    }
    
    /**
     * Creates a new {@link IFormulaToCnfConverter} with the given strategy.
     * 
     * @param strategy The strategy to use.
     * @return A {@link IFormulaToCnfConverter} using the given strategy.
     */
    public static IFormulaToCnfConverter create(Strategy strategy) {
        IFormulaToCnfConverter result;
        
        switch (strategy) {
        case RECURISVE:
            result = new RecursiveCnfConverter();
            break;
            
        case RECURISVE_REPLACING:
            result = new RecursiveReplacingCnfConverter();
            break;
            
        default:
            result = null;
            break;
        }
        
        return result;
    }
    
    /**
     * Creates a new {@link IFormulaToCnfConverter} with the default strategy.
     * The default strategy is {@link Strategy#RECURISVE}.
     * 
     * @return A {@link IFormulaToCnfConverter} using the default strategy.
     */
    public static IFormulaToCnfConverter create() {
        return create(Strategy.RECURISVE);
    }
    
}
