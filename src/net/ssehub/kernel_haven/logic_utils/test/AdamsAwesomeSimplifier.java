package net.ssehub.kernel_haven.logic_utils.test;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.logic_utils.LogicUtils;
import net.ssehub.kernel_haven.util.PerformanceProbe;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.FormulaSimplifier;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Adam's awesome simplification heuristic. This heuristic to shorten {@link Formula}s performs the following steps:
 * <ol>
 *      <li>Prune all constants</li>
 *      <li>Move negations inwards (as much as possible)</li>
 *      <li>Call simple simplification ({@link LogicUtils#simplifyWithVisitor(Formula)})</li>
 *      <li>Call sub-tree based simplification ({@link SubTreeSimplifier})</li>
 *      <li>Move negations outwards (were possible)</li>
 *      <li>Call sub-tree based simplification ({@link SubTreeSimplifier})</li>
 * </ol>
 *
 * @author Adam
 */
public class AdamsAwesomeSimplifier {
    
    private static final int NUM_ITERATIONS_SAME;
    
    static {
        String setting = System.getProperty("AAS_NUM_ITERATIONS_SAME");
        if (setting == null) {
            NUM_ITERATIONS_SAME = 3;
        } else {
            NUM_ITERATIONS_SAME = Integer.parseInt(setting);
        }
    }
    
    /**
     * Moves all {@link Negation}s inwards as much as possible. After this, negations only occur around
     * {@link Variable}s, {@link True} and {@link False}.
     * 
     * @param formula The formula to move the negation inwards for.
     * 
     * @return The formula with moved negations.
     */
    private static @NonNull Formula moveNegationInwards(@NonNull Formula formula) {
        
        Formula result;
        
        if (formula instanceof Disjunction) {
            Disjunction dis = (Disjunction) formula;
            result = new Disjunction(moveNegationInwards(dis.getLeft()), moveNegationInwards(dis.getRight()));
            
        } else if (formula instanceof Conjunction) {
            Conjunction con = (Conjunction) formula;
            result = new Conjunction(moveNegationInwards(con.getLeft()), moveNegationInwards(con.getRight()));
            
        } else if (formula instanceof Negation) {
            Formula nested = ((Negation) formula).getFormula();
            
            if (nested instanceof Disjunction) {
                Disjunction dis = (Disjunction) nested;
                result = and(moveNegationInwards(not(dis.getLeft())), moveNegationInwards(not(dis.getRight())));
                
            } else if (nested instanceof Conjunction) {
                Conjunction con = (Conjunction) nested;
                result = or(moveNegationInwards(not(con.getLeft())), moveNegationInwards(not(con.getRight())));
                
            } else {
                result = formula;
            }
            
        } else {
            result = formula;
        }
        
        return result;
    }
    
    /**
     * Moves {@link Negation}s outwards, if possible. For example, this converts <code>!A || !B</code> to
     * <code>!(A && B)</code>.
     * 
     * @param formula The formula to move negations for.
     * 
     * @return The formula with moved negations.
     */
    private static @NonNull Formula moveNegationOutwards(@NonNull Formula formula) {
        Formula result;
        
        if (formula instanceof Disjunction) {
            Disjunction dis = (Disjunction) formula;
            List<@NonNull Formula> allTerms = new LinkedList<>();
            FormulaStructureChecker.getAllDisjunctionTerms(dis).stream()
                    .map((term) -> moveNegationOutwards(term))
                    .forEach(allTerms::add);
            
            boolean allNegated = true;
            for (Formula f : allTerms) {
                if (!(f instanceof Negation)) {
                    allNegated = false;
                    break;
                }
            }
            
            if (allNegated) {
                result = ((Negation) notNull(allTerms.remove(0))).getFormula();
                for (Formula f : allTerms) {
                    result = and(result, ((Negation) f).getFormula());
                }
                result = new Negation(result);
                
            } else {
                result = notNull(allTerms.remove(0));
                for (Formula term : allTerms) {
                    result = or(result, term);
                }
            }
            
        } else if (formula instanceof Conjunction) {
            Conjunction con = (Conjunction) formula;
            List<@NonNull Formula> allTerms = new LinkedList<>();
            FormulaStructureChecker.getAllConjunctionTerms(con).stream()
                    .map((term) -> moveNegationOutwards(term))
                    .forEach(allTerms::add);
            
            boolean allNegated = true;
            for (Formula f : allTerms) {
                if (!(f instanceof Negation)) {
                    allNegated = false;
                    break;
                }
            }
            
            if (allNegated) {
                result = ((Negation) notNull(allTerms.remove(0))).getFormula();
                for (Formula f : allTerms) {
                    result = or(result, ((Negation) f).getFormula());
                }
                result = new Negation(result);
                
            } else {
                result = notNull(allTerms.remove(0));
                for (Formula term : allTerms) {
                    result = and(result, term);
                }
            }
            
        } else if (formula instanceof Negation) {
            result = new Negation(moveNegationOutwards(((Negation) formula).getFormula()));
            
        } else {
            result = formula;
        }
        
        return result;
    }
    
    /**
     * Runs this simplification heuristic on the given formula.
     * 
     * @param formula The formula to simplify.
     * 
     * @return The simplified formula.
     */
    private static @NonNull Formula simplifyImpl(@NonNull Formula formula) {
        PerformanceProbe p;
        
        // Step 1: prune all constants
        p = new PerformanceProbe("AAS: 1) Prune Constants");
        formula = FormulaSimplifier.defaultSimplifier(formula);
        p.close();
        
        
        // Step 2: move all negations inwards
        p = new PerformanceProbe("AAS: 2) Move Negations Inward");
        formula = moveNegationInwards(formula);
        p.close();
        
        // Step 3: simple simplification
        p = new PerformanceProbe("AAS: 3) Simple Simplification");
        formula = LogicUtils.simplifyWithVisitor(formula);
        p.close();
        
        // Step 4: sub-tree group simplifier
        p = new PerformanceProbe("AAS: 4) Sub-Tree Simplifier");
        formula = SubTreeSimplifier.simplify(formula);
        p.close();
        
        // Step 5: move negations outward again (where applicable)
        p = new PerformanceProbe("AAS: 5) Move Negations Outward");
        formula = moveNegationOutwards(formula);
        p.close();
        
        // Step 6: sub-tree group simplifier
        p = new PerformanceProbe("AAS: 6) Sub-Tree Simplifier");
        formula = SubTreeSimplifier.simplify(formula);
        p.close();
        
        return formula;
    }
    
    /**
     * Runs this simplification heuristic on the given formula.
     * 
     * @param formula The formula to simplify.
     * 
     * @return The simplified formula.
     */
    public static @NonNull Formula simplify(@NonNull Formula formula) {
        int shortestLength = formula.toString().length();
        Formula shortest = formula;
        
        int previousLength;
        int numItersSame = 0;
        do {
            previousLength = formula.toString().length();
            formula = simplifyImpl(formula);
            int currentLength = formula.toString().length();
            
            if (currentLength < shortestLength) {
                shortestLength = currentLength;
                shortest = formula;
            }
            
            if (previousLength <= currentLength) {
                numItersSame++;
            } else {
                numItersSame = 0;
            }
        } while (numItersSame < NUM_ITERATIONS_SAME);
        
        return shortest;
    }

}
