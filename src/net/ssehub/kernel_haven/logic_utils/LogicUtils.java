package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.EnumSetting;
import net.ssehub.kernel_haven.logic_utils.test.AdamsAwesomeSimplifier;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.PerformanceProbe;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.FormulaSimplifier;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Utility functions for {@link Formula}s.
 * 
 * @author Adam
 * @author El-Sharkawy
 */
public class LogicUtils {

    /**
     * A setting to define which simplification to use.
     */
    public static final @NonNull EnumSetting<@NonNull Simplification> SIMPLIFICATION_SETTING
            = new EnumSetting<>("logic.simplifier", Simplification.class, true, Simplification.VISITOR,
            "Specifies which heuristic to use to simplify Boolean expressions.");
    
    /**
     * Different simplification strategies to use.
     */
    public static enum Simplification {
        
        /**
         * Only use the default simplification of the main infrastructure. Basically only prunes constants.
         */
        SIMPLE,
        
        /**
         * Use the <a href="https://github.com/bpodgursky/jbool_expressions">jbool_expressions</a> library.
         */
        LIBRARY,
        
        /**
         * Use the {@link FormulaSimplificationVisitor}.
         */
        VISITOR,
        
        /**
         * Use {@link AdamsAwesomeSimplifier} (utilizes all of the above).
         */
        ADAMS_AWESOME_SIMPLIFIER;
        
    }
    
    private static final Logger LOGGER = Logger.get();
    
    
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
    public static @NonNull Formula simplifyWithLibrary(@NonNull Formula formula) {
        FormulaToExpressionConverter converter = new FormulaToExpressionConverter();
        Expression<String> expr = converter.visit(formula);
        Expression<String> translated = notNull(RuleSet.simplify(expr));
        
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
                LOGGER.logExceptionInfo("Could not simplifiy formula: " + formula.toString(), e);
                // Keep old result (even if it can be simplified)
                result = formula;
            }
        }
        return result;
    }
    
    
    /**
     * Simplifies the given {@link Formula} with the {@link FormulaSimplificationVisitor}. The semantics do not change.
     * 
     * @param formula The formula to simplify. Must not be <code>null</code>.
     * 
     * @return The simplified formula. Not <code>null</code>.
     */
    public static @NonNull Formula simplifyWithVisitor(@NonNull Formula formula) {
        PerformanceProbe p;
        
        p = new PerformanceProbe("VisitorSimplifier");
        formula = formula.accept(new FormulaSimplificationVisitor());
        p.close();
        
        return formula;
    }
    
    /**
     * Initialization method called by KernelHaven. See loadClasses.txt
     * 
     * @param config The global pipeline configuration.
     * 
     * @throws SetUpException If the {@link Configuration} doesn't contain a valid setting for the simplification.
     */
    public static void initialize(@NonNull Configuration config) throws SetUpException {
        config.registerSetting(SIMPLIFICATION_SETTING);
        
        switch (config.getValue(SIMPLIFICATION_SETTING)) {
        case SIMPLE:
            // do nothing; stay with default simplifier from the infrastructure
            break;
            
        case LIBRARY:
            FormulaSimplifier.setSimplifier(LogicUtils::simplifyWithLibrary);
            break;
            
        case VISITOR:
            FormulaSimplifier.setSimplifier(LogicUtils::simplifyWithVisitor);
            break;
            
        case ADAMS_AWESOME_SIMPLIFIER:
            FormulaSimplifier.setSimplifier(AdamsAwesomeSimplifier::simplify);
            break;
        
        default:
            throw new SetUpException("Unexpected simplification type: " + config.getValue(SIMPLIFICATION_SETTING));
        }
    }
    
}
