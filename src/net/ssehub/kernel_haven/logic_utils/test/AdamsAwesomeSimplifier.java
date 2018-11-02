package net.ssehub.kernel_haven.logic_utils.test;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.ssehub.kernel_haven.cnf.ConverterException;
import net.ssehub.kernel_haven.cnf.SolverException;
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
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
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
    
    private static boolean debug = false;
    
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
     * Helper method for printing debug information.
     * 
     * @param formula The formula to print.
     * @param title The title to print.
     */
    private static void debugPrintFormula(@NonNull Formula formula, @NonNull String title) {
        if (debug) {
            System.out.println(title);
            System.out.println("-------------------------------".substring(0, title.length()));
            String str = formula.toString();
            System.out.println(str.length() + " chars");
            System.out.println(str);
//        System.out.println(new FormulaTreePrinter().visit(formula));
            System.out.println();
        }
    }
    
    /**
     * Runs this simplification heuristic on the given formula.
     * 
     * @param formula The formula to simplify.
     * 
     * @return The simplified formula.
     */
    public static @NonNull Formula simplify(@NonNull Formula formula) {
        debugPrintFormula(formula, "Step 0: Original");

        PerformanceProbe p;
        
        // Step 1: prune all constants
        p = new PerformanceProbe("AAS: 1) Prune Constants");
        formula = FormulaSimplifier.defaultSimplifier(formula);
        debugPrintFormula(formula, "Step 1: Pruned Constants");
        p.close();
        
        
        // Step 2: move all negations inwards
        p = new PerformanceProbe("AAS: 2) Move Negations Inward");
        formula = moveNegationInwards(formula);
        debugPrintFormula(formula, "Step 2: Negations Moved In");
        p.close();
        
        // Step 3: simple simplification
        p = new PerformanceProbe("AAS: 3) Simple Simplification");
        formula = LogicUtils.simplifyWithVisitor(formula);
        debugPrintFormula(formula, "Step 3: Simple Simplificiaton");
        p.close();
        
        // Step 4: sub-tree group simplifier
        p = new PerformanceProbe("AAS: 4) Sub-Tree Simplifier");
        formula = SubTreeSimplifier.simplify(formula);
        debugPrintFormula(formula, "Step 4: Sub-tree Simplifier");
        p.close();
        
        // Step 5: move negations outward again (where applicable)
        p = new PerformanceProbe("AAS: 5) Move Negations Outward");
        formula = moveNegationOutwards(formula);
        debugPrintFormula(formula, "Step 5: Negations Moved Out");
        p.close();
        
        // Step 6: sub-tree group simplifier
        p = new PerformanceProbe("AAS: 6) Sub-Tree Simplifier");
        formula = SubTreeSimplifier.simplify(formula);
        debugPrintFormula(formula, "Step 6: Sub-tree Simplifier");
        p.close();
        
        // Step 7: sub-tree group simplifier
        p = new PerformanceProbe("AAS: 7) Sub-Tree Simplifier");
        formula = SubTreeSimplifier.simplify(formula);
        debugPrintFormula(formula, "Step 7: Sub-tree Simplifier");
        p.close();
        
        return formula;
    }

    /**
     * Helper method for generating random {@link Formula}s. This generates approximately 2000 character long formulas.
     * 
     * @param random A random source.
     * @param depth The current depth. Start with 0.
     * 
     * @return A random {@link Formula}.
     */
    private static @NonNull Formula generateRandomFormula(@NonNull Random random, int depth) {
        Formula result;
        
        if (depth > 10) {
            result = new Variable("VAR_" + random.nextInt(10));
            
        } else {
            
            int rand = random.nextInt(100);
            
            
            if (rand < 30) {
                result = or(generateRandomFormula(random, depth + 1), generateRandomFormula(random, depth + 1));
                
            } else if (rand < 60) {
                result = and(generateRandomFormula(random, depth + 1), generateRandomFormula(random, depth + 1));
                
            } else if (rand < 75) {
                result = new Negation(generateRandomFormula(random, depth + 1));
                
            } else if (depth > 4 && rand < 95) {
                result = new Variable("VAR_" + random.nextInt(10));
                
            } else if (depth > 4 && rand < 98) {
                result = True.INSTANCE;
                
            } else if (depth > 4 && rand < 100) {
                result = False.INSTANCE;
                
            } else {
                result = and(generateRandomFormula(random, depth + 1), generateRandomFormula(random, depth + 1));
            }
        }
        
        
        return result;
        
    }
    
    /**
     * Temporary test method.
     * 
     * @param args ignored.
     * @throws ExpressionFormatException .
     * @throws SolverException .
     * @throws ConverterException .
     */
    public static void main(String[] args) throws ExpressionFormatException, SolverException, ConverterException {
//        Parser<@NonNull Formula> parser = new Parser<>(new CStyleBooleanGrammar(new VariableCache()));
//        Formula f = parser.parse("((A || B) && C) || ((A || B) && !C)");
//        Formula f = parser.parse("(D || E || (F && (D || E))) && (!(D || E) || !(F && (D || E)))");
//        Formula f = parser.parse("(G || H || ((G || H) && (X_1 || X_2 || X_45))) && (!(G || H) || !((G || H) && (X_1 "
//                + "|| X_2 || X_45)))");
        
        for (int i = 0; i < 1000; i++) {
            Formula f = generateRandomFormula(new Random(), 0);
//            Formula original = f;
            
//        debug = true;
            simplify(f);
            
        }
        
//        System.out.println();
//        System.out.println("Logically equal? " + new FormulaEqualityChecker().isLogicallyEqual(f, original));
        
        
        PerformanceProbe.printResult();
    }

}
