package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.IFormulaVisitor;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A utility class for converting {@link Formula}s to strings representing their tree structure. Not thread safe.
 *
 * @author Adam
 */
public class FormulaTreePrinter implements IFormulaVisitor<@NonNull String> {

    private @NonNull String currentNestingDepth;
    
    /**
     * Creates a {@link FormulaTreePrinter}.
     */
    public FormulaTreePrinter() {
        currentNestingDepth = "";
    }
    
    /**
     * Increases the current nesting depth.
     */
    private void increaseNesting() {
        currentNestingDepth += "\t";
    }
    
    /**
     * Decreases the current nesting depth.
     */
    private void decreaseNesting() {
        currentNestingDepth = notNull(currentNestingDepth.substring(1));
    }
    
    @Override
    public @NonNull String visitFalse(@NonNull False falseConstant) {
        return currentNestingDepth + "false";
    }

    @Override
    public @NonNull String visitTrue(@NonNull True trueConstant) {
        return currentNestingDepth + "true";
    }

    @Override
    public @NonNull String visitVariable(@NonNull Variable variable) {
        return currentNestingDepth + variable.getName();
    }

    @Override
    public @NonNull String visitNegation(@NonNull Negation formula) {
        StringBuilder result = new StringBuilder();
        
        result.append(currentNestingDepth).append("!(\n");
        increaseNesting();
        result.append(formula.getFormula().accept(this)).append("\n");
        decreaseNesting();
        result.append(currentNestingDepth).append(")");
        
        
        return notNull(result.toString());
    }

    @Override
    public @NonNull String visitDisjunction(@NonNull Disjunction formula) {
        StringBuilder result = new StringBuilder();
        
        increaseNesting();
        result.append(formula.getLeft().accept(this)).append("\n");
        decreaseNesting();
        
        result.append(currentNestingDepth).append("||\n");
        
        increaseNesting();
        result.append(formula.getRight().accept(this));
        decreaseNesting();
        
        return notNull(result.toString());
    }

    @Override
    public @NonNull String visitConjunction(@NonNull Conjunction formula) {
        StringBuilder result = new StringBuilder();
        
        increaseNesting();
        result.append(formula.getLeft().accept(this)).append("\n");
        decreaseNesting();
        
        result.append(currentNestingDepth).append("&&\n");
        
        increaseNesting();
        result.append(formula.getRight().accept(this));
        decreaseNesting();
        
        return notNull(result.toString());
    }

}
