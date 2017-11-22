package net.ssehub.kernel_haven.logic_utils;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import net.ssehub.kernel_haven.util.logic.Formula;

/**
 * Utility functions for {@link Formula}s.
 * 
 * @author Adam
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
        Expression<String> expr = new FormulaToExpressionConverter().visit(formula);
        expr = RuleSet.simplify(expr);
        return FormulaToExpressionConverter.expressionToFormula(expr);
    }
    
}
