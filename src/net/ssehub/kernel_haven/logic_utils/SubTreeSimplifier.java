package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.logic_utils.FormulaStructureChecker.isStructurallyEqual;
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.PerformanceProbe;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A simplifier based on sub-tree occurrences. This simplifier finds sub-trees that occur more than one time, replaces
 * them with a temporary variable, and runs simplification on this simpler formula. This is useful, because the
 * standard simplifiers we use tend to find patterns with only variables better than patterns involving complex
 * sub-trees. After the simplification, the temporary replacement variable is replaced with the original sub-tree. This
 * process is repeated until no further simplification can be found.
 *
 * @author Adam
 */
public class SubTreeSimplifier {

    private static final int MAX_ITERATIONS = 50;

    /**
     * Don't allow any instances.
     */
    private SubTreeSimplifier() {
    }
    
    /**
     * Replaces all elements in toReplace found in formula with replacement.
     * 
     * @param formula The formula to search in.
     * @param toReplace All instances that need to be replaced.
     * @param replacement The replacement to use in place of the toReplace instances.
     * 
     * @return The formula with all replacements done.
     */
    private static @NonNull Formula replaceAll(@NonNull Formula formula, List<@NonNull Formula> toReplace,
            @NonNull Formula replacement) {

        Formula result = formula;

//        if (toReplace.stream().filter((element) -> element == formula).findAny().isPresent()) {
        if (toReplace.contains(formula)) {
            result = replacement;
            
        } else {

            if (formula instanceof Disjunction) {
                Disjunction dis = (Disjunction) formula;
                
                Formula left = replaceAll(dis.getLeft(), toReplace, replacement);
                Formula right = replaceAll(dis.getRight(), toReplace, replacement);
                
                if (left != dis.getLeft() || right != dis.getRight()) {
                    // only create new object if children actually changed
                    result = new Disjunction(left, right);
                }

            } else if (formula instanceof Conjunction) {
                Conjunction con = (Conjunction) formula;
                
                Formula left = replaceAll(con.getLeft(), toReplace, replacement);
                Formula right = replaceAll(con.getRight(), toReplace, replacement);
                
                if (left != con.getLeft() || right != con.getRight()) {
                    // only create new object if children actually changed
                    result = new Conjunction(left, right);
                }
                
            } else if (formula instanceof Negation) {
                Negation neg = (Negation) formula;
                
                Formula nested = replaceAll(neg.getFormula(), toReplace, replacement);
                
                if (nested != neg.getFormula()) {
                    // only create new object if children actually changed
                    result = new Negation(nested);
                }
            }

        }
        
        return result;

    }

    /**
     * Runs this simplification approach on the given {@link Formula}.
     * 
     * @param formula The formula to simplify.
     * 
     * @return The simplified formula.
     */
    public static @NonNull Formula simplify(@NonNull Formula formula) {

        PerformanceProbe p;
        SubTreeGroupFinder subTreeFinder = new SubTreeGroupFinder();

        int iteration = 0;
        boolean changed;
        do {
            changed = false;
            
            if (iteration == MAX_ITERATIONS) {
                Logger.get().logWarning("Stopping simplification after " + MAX_ITERATIONS + " iterations");
                break;
            }
            
            p = new PerformanceProbe("SubTreeSimplifier 1) Find Trees");
            List<@NonNull List<@NonNull Formula>> trees = new LinkedList<>();
            subTreeFinder.findGroups(formula).stream()
                    .filter((list) -> list.size() > 1)
                    
                    // sort by descending number of sub-trees
//                     .sorted((l1, l2) -> Integer.compare(l2.size(), l1.size())) 
                    
                    // sort descending by size of sub-tree
                    // this seems to perform better than the above (needs fewer overall iterations)
                    .sorted((l1, l2) ->
                        Integer.compare(notNull(l2.get(0)).toString().length(), notNull(l1.get(0)).toString().length()))
                    
                    .forEach(trees::add);
            p.close();

            PerformanceProbe p2 = new PerformanceProbe("SubTreeSimplifier 2) Replacing & Simplifying");
            Variable replacement = new Variable("__TMP_REPLACE__");
            for (List<@NonNull Formula> subTreeList : trees) {

                p = new PerformanceProbe("SubTreeSimplifier 2.1) Replace");
                Formula withRepl = replaceAll(formula, subTreeList, replacement);
                p.close();
                
                p = new PerformanceProbe("SubTreeSimplifier 2.2) Simplify");
                Formula withReplSimpl = new FormulaSimplificationVisitor2().visit(withRepl);
                p.close();

                p = new PerformanceProbe("SubTreeSimplifier 2.3) Check");
                boolean thisIterationChanged = !isStructurallyEqual(withRepl, withReplSimpl);
                changed |= thisIterationChanged;
                p.close();
                
                if (thisIterationChanged) {
                    p = new PerformanceProbe("SubTreeSimplifier 2.4) Re-replace");
                    formula = replaceAll(withReplSimpl, Arrays.asList(replacement), subTreeList.get(0));
                    p.close();
                }
            }
            p2.close();
            
        } while (changed);

        return formula;
    }
    
}
