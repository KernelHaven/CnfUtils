package net.ssehub.kernel_haven.logic_utils;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.logic.Formula;

/**
 * Utility functions for {@link Formula}s.
 * 
 * @author Adam
 * @author El-Sharkawy
 */
public class LogicUtils {

    /**
     * Don't allow instances.
     */
    private LogicUtils() {
    }
    
    /**
     * Simplifies the given {@link Formula}. The semantics do not change.
     * 
     * @param formula The formula to simplify. Must not be <code>null</code>.
     * 
     * @return The simplified formula. Not <code>null</code>.
     * @see <a href="https://github.com/bpodgursky/jbool_expressions">
     * https://github.com/bpodgursky/jbool_expressions</a>
     */
    public static Formula simplify(Formula formula) {
        FormulaToExpressionConverter converter = new FormulaToExpressionConverter();
        Expression<String> expr = converter.visit(formula);
        Expression<String> translated = RuleSet.simplify(expr);
        
        // Re-translate and return simplified element only if there was something to simplify
        Formula result;
        if (expr.equals(translated)) {
            // Optimization: If nothing was simplified, do not re-translate expression
            result = formula;
        } else {
            // There was an optimization
            try {
                result = converter.expressionToFormula(translated);
            } catch (FormatException e) {
                Logger.get().logExceptionInfo("Could not simplifiy formula: " + formula.toString(), e);
                // Keep old result (even if it can be simplified)
                result = formula;
            }
        }
        return result;
    }
    
}
