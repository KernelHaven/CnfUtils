package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * <p>
 * Tests if two {@link Formula}s are structurally equal.
 * </p>
 * <p>
 * The following examples are structurally equal formulas that are detected by this checker:
 * <ul>
 *      <li><code>A || B</code> equals <code>B || A</code> (left- and right-hand-side may be reversed)</li>
 *      <li><code>A || (B || C)</code> equals <code>(A || B) || C</code> (nesting structure may be changed)</li>
 *      <li><code>A || (B || C)</code> equals <code>(B || A) || C</code> (nesting structure and order  may be changed)
 *      </li>
 * </ul>
 * </p>
 * 
 * @author Adam
 */
public class FormulaStructureChecker {

    /**
     * Don't allow any instances.
     */
    private FormulaStructureChecker() {
    }

    /**
     * Checks if the two given {@link Formula}s are structurally equal.
     * 
     * @param f1 The first formula.
     * @param f2 The second formula.
     * 
     * @return Whether the two {@link Formula}s are structurally equal.
     */
    public static boolean isStructurallyEqual(@NonNull Formula f1, @NonNull Formula f2) {
        boolean result = false;
        if (f1.getClass() == f2.getClass()) {
            
            if (f1 instanceof Variable || f1 instanceof True || f1 instanceof False) {
                result = f1.equals(f2);
            } else if (f1 instanceof Negation) {
                result = isStructurallyEqual(((Negation) f1).getFormula(), ((Negation) f2).getFormula());
            } else if (f1 instanceof Disjunction) {
                result = checkDisjunction((Disjunction) f1, (Disjunction) f2);
            } else if (f1 instanceof Conjunction) {
                result = checkConjunction((Conjunction) f1, (Conjunction) f2);
            }
            
        }
        
        return result;
    }
    
    /**
     * Creates a list of all terms that are in the given {@link Disjunction}. This also considers further
     * {@link Disjunction}s that are nested elements of the given {@link Disjunction}. E.g., the formula
     * <code>(A || B) || (C && D)</code> will return the list <code>[A, B, C && D]</code>.
     * 
     * @param dis The disjunction to get all terms of (including child {@link Disjunction}s).
     * 
     * @return The list of all terms that are combined with a disjunction.
     */
    public static @NonNull List<@NonNull Formula> getAllDisjunctionTerms(@NonNull Disjunction dis) {
        List<@NonNull Formula> toCheck = new LinkedList<>();
        toCheck.add(dis.getLeft());
        toCheck.add(dis.getRight());
        
        boolean foundDisjunction;
        
        do {
            foundDisjunction = false;
            
            List<@NonNull Formula> toAdd = new LinkedList<>();
            
            Iterator<@NonNull Formula> it = toCheck.iterator();
            while (it.hasNext()) {
                Formula f = notNull(it.next());
                if (f instanceof Disjunction) {
                    foundDisjunction = true;
                    it.remove();
                    
                    toAdd.add(((Disjunction) f).getLeft());
                    toAdd.add(((Disjunction) f).getRight());
                }
            }
            
            toCheck.addAll(toAdd);
            
        } while (foundDisjunction);
        
        return toCheck;
    }
    
    /**
     * Checks if the two given lists of terms are structurally equal. The term-order does not matter.
     * 
     * @param toCheckL The first list to check.
     * @param toCheckR The second list to check.
     * 
     * @return Whether the two term lists are equal.
     */
    private static boolean termsAreEqual(@NonNull List<@NonNull Formula> toCheckL,
            @NonNull List<@NonNull Formula> toCheckR) {
        
        boolean result;
        
        if (toCheckL.size() != toCheckR.size()) {
            result = false;
            
        } else {
            
            for (Formula left : toCheckL) {
                
                boolean found = false;
                Iterator<@NonNull Formula> itR = toCheckR.iterator();
                while (itR.hasNext()) {
                    Formula right = notNull(itR.next());
                    
                    if (isStructurallyEqual(left, right)) {
                        itR.remove();
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    break;
                }
            }
            
            result = toCheckR.isEmpty();
        }
        
        
        return result;
    }
    
    /**
     * Checks if the two given {@link Disjunction}s are structurally equal.
     * 
     * @param f1 The first disjunction.
     * @param f2 The second disjunction.
     * 
     * @return Whether the two disjunctions are structurally equal.
     */
    private static boolean checkDisjunction(@NonNull Disjunction f1, @NonNull Disjunction f2) {
        return termsAreEqual(getAllDisjunctionTerms(f1), getAllDisjunctionTerms(f2));
    }
    
    /**
     * Creates a list of all terms that are in the given {@link Conjunction}. This also considers further
     * {@link Conjunction}s that are nested elements of the given {@link Conjunction}. E.g., the formula
     * <code>(A && B) && (C || D)</code> will return the list <code>[A, B, C || D]</code>.
     * 
     * @param con The conjunction to get all terms of (including child {@link Conjunction}s).
     * 
     * @return The list of all terms that are combined with a conjunction.
     */
    public static @NonNull List<@NonNull Formula> getAllConjunctionTerms(@NonNull Conjunction con) {
        List<@NonNull Formula> toCheck = new LinkedList<>();
        toCheck.add(con.getLeft());
        toCheck.add(con.getRight());
        
        boolean foundConjunction;
        
        do {
            foundConjunction = false;
            
            List<@NonNull Formula> toAdd = new LinkedList<>();
            
            Iterator<@NonNull Formula> it = toCheck.iterator();
            while (it.hasNext()) {
                Formula f = notNull(it.next());
                if (f instanceof Conjunction) {
                    foundConjunction = true;
                    it.remove();
                    
                    toAdd.add(((Conjunction) f).getLeft());
                    toAdd.add(((Conjunction) f).getRight());
                }
            }
            
            toCheck.addAll(toAdd);
            
        } while (foundConjunction);
        
        return toCheck;
    }
    
    /**
     * Checks if the two given {@link Conjunction}s are structurally equal.
     * 
     * @param f1 The first conjunction.
     * @param f2 The second conjunction.
     * 
     * @return Whether the two conjunctions are structurally equal.
     */
    private static boolean checkConjunction(@NonNull Conjunction f1, @NonNull Conjunction f2) {
        return termsAreEqual(getAllConjunctionTerms(f1), getAllConjunctionTerms(f2));
    }

}
