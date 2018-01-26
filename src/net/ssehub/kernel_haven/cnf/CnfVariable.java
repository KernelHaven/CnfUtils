package net.ssehub.kernel_haven.cnf;

import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Represents an element of the CNF combining a negation (<b>true if negated</b>
 * ) and the name of the variable.
 * 
 * @author Kevin
 * @author Johannes
 * @author Malek
 * @author Alice
 * @author Adam
 */
public class CnfVariable {

    /**
     * The negation of the variables. Not null. Default Value = false.
     */
    private boolean negation;

    /**
     * The name of the variables. Not null.
     */
    private @NonNull String name;

    /**
     * Instanciates a new cnf Variable.
     * 
     * @param negation
     *            represents the NOT part of the variable. Must not be null
     *            because primitive.
     * @param name
     *            represents the variable. Can be null.
     */
    public CnfVariable(boolean negation, @NonNull String name) {
        this.negation = negation;
        this.name = name;
    }

    /**
     * Instantiates a new cnf variable.
     *
     * @param name
     *            is the name of the cnf variable. Must not be null.
     */
    public CnfVariable(@NonNull String name) {
        this.negation = false;
        this.name = name;
    }

    /**
     * Checks if the cnf variable is not negated. True = The variable is
     * negated.
     *
     * @return true or false if the cnf variable is negated or not.
     */
    public boolean isNegation() {
        return negation;
    }

    /**
     * Gets the name of the cnf variable.
     *
     * @return the name of the cnf variable. Not null.
     */
    public @NonNull String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + new Boolean(negation).hashCode();
    }
    
    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;
        if (obj instanceof CnfVariable) {
            CnfVariable other = (CnfVariable) obj;
            result = other.name.equals(this.name) && other.negation == this.negation;
        }
        return result;
    }
    
    @Override
    public @NonNull String toString() {
        return (negation ? "!" : "") + name;
    }

}
