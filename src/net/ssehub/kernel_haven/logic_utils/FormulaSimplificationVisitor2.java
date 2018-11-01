package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.ssehub.kernel_haven.logic_utils.test.FormulaStructureChecker;
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
 * <p>
 * Creates a more concise formula based on the visited input formula.
 * If the formula cannot be simplified, the same instance is (sometimes) returned.
 * </p>
 * <p>
 * Applies rules of the <a href="https://en.wikipedia.org/wiki/Boolean_algebra#Laws">Boolean algebra</a> and
 * combinations of these rules.
 * </p>
 * <p>
 * This is based on {@link FormulaSimplificationVisitor}, but it uses
 * {@link FormulaStructureChecker#getAllConjunctionTerms(Conjunction)} and
 * {@link FormulaStructureChecker#getAllDisjunctionTerms(Disjunction)} to flatten dis- and conjunction hierarchies
 * before applying the rules.
 * </p>
 * 
 * @author Adam
 * @author El-Sharkawy
 */
public class FormulaSimplificationVisitor2 implements IFormulaVisitor<@NonNull Formula> {

    @Override
    public Formula visitFalse(@NonNull False falseConstant) {
        return falseConstant;
    }

    @Override
    public Formula visitTrue(@NonNull True trueConstant) {
        return trueConstant;
    }

    @Override
    public Formula visitVariable(@NonNull Variable variable) {
        return variable;
    }

    @Override
    public Formula visitNegation(@NonNull Negation formula) {
        Formula inner = formula.getFormula().accept(this);

        Formula result;
        
        if (inner instanceof Negation) {
            // Double negation
            result = ((Negation) inner).getFormula();
        } else if (inner instanceof True) {
            result = False.INSTANCE;
        } else if (inner instanceof False) {
            result = True.INSTANCE;
        } else {
            // only create new instance if nested changed
            if (inner != formula.getFormula()) {
                result = new Negation(inner);
            } else {
                result = formula;
            }
        }
        
        return result;
    }


    // CHECKSTYLE:OFF // TODO method too long
    @Override
    public Formula visitDisjunction(@NonNull Disjunction formula) {
    // CHECKSTYLE:ON
        List<Formula> terms = new ArrayList<>();
        
        AtomicBoolean containsTrue = new AtomicBoolean(false);
        FormulaStructureChecker.getAllDisjunctionTerms(formula).stream()
                .map((term) -> term.accept(this))
                .forEach((term) -> {
                    if (term == True.INSTANCE) {
                        containsTrue.set(true);
                    } else if (term != False.INSTANCE) {
                        terms.add(term);
                    }
                });
        
        if (containsTrue.get()) {
            return True.INSTANCE;
        }
        
        if (terms.isEmpty()) {
            return False.INSTANCE; // we didn't find a single non-false item
        }
        
        for (int li = 0; li < terms.size(); li++) {
            for (int ri = li + 1; ri < terms.size(); ri++) {
                Formula left = notNull(terms.get(li)); 
                Formula right = notNull(terms.get(ri)); 
                
                if (left.equals(right)) {
                    // Idempotence: A v A -> A
                    // remove right term and continue
                    terms.remove(ri);
                    ri--;
                } else if (left instanceof Negation && ((Negation) left).getFormula().equals(right)) {
                    // Complementation: !A v A -> true
                    // whole disjunction becomes true
                    return True.INSTANCE;
                } else if (right instanceof Negation && ((Negation) right).getFormula().equals(left)) {
                    // Complementation: A v !A -> true
                    // whole disjunction becomes true
                    return True.INSTANCE;
                } else if (left instanceof Variable && right instanceof Conjunction
                            && isOrAbsorption((Variable) left, (Conjunction) right)) {
                    // Classical Absorption: A v (A ^ B) -> A
                    // remove right term and continue
                    terms.remove(ri);
                    ri--;
                } else if (right instanceof Variable && left instanceof Conjunction
                        && isOrAbsorption((Variable) right, (Conjunction) left)) {
                    // Classical Absorption: (A ^ B) v A -> A
                    // remove left term and continue
                    terms.remove(li);
                    li--;
                    break; // break inner loop, since we modified li
                } else if (left instanceof Negation && right instanceof Negation) {
                    Formula nestedLeft = ((Negation) left).getFormula();
                    Formula nestedRight = ((Negation) right).getFormula();
                    
                    if (nestedLeft instanceof Variable && nestedRight instanceof Disjunction) {
                        // Check for combination of complementation, De Morgan, Identity
                        Formula tmp = negatedOrComplementation((Negation) left, (Disjunction) nestedRight);
                        
                        if (tmp != null) {
                            // tmp replaces ri
                            terms.set(ri, tmp);
                            li = -1; // restart
                            break;
                        }
                        
                    } else if (nestedRight instanceof Variable && nestedLeft instanceof Disjunction) {
                        // Check for combination of complementation, De Morgan, Identity
                        Formula tmp = negatedOrComplementation((Negation) right, (Disjunction) nestedLeft);
                        
                        if (tmp != null) {
                            // tmp replaces li
                            terms.set(li, tmp);
                            li = -1; // restart
                            break;
                        }
                        
                    }
                }
                
            }
        }
        
        Formula result = notNull(terms.get(0));
        for (int i = 1; i < terms.size(); i++) {
            result = new Disjunction(result, notNull(terms.get(i)));
        }
        
        return result;
    }

    // CHECKSTYLE:OFF
    @Override
    public Formula visitConjunction(@NonNull Conjunction formula) {
    // CHECKSTYLE:ON
        List<Formula> terms = new ArrayList<>();
        
        AtomicBoolean containsFalse = new AtomicBoolean(false);
        FormulaStructureChecker.getAllConjunctionTerms(formula).stream()
                .map((term) -> term.accept(this))
                .forEach((term) -> {
                    if (term == False.INSTANCE) {
                        containsFalse.set(true);
                    } else if (term != True.INSTANCE) {
                        terms.add(term);
                    }
                });

        if (containsFalse.get()) {
            return False.INSTANCE;
        }
        
        if (terms.isEmpty()) {
            return True.INSTANCE; // we didn't find a single non-true item
        }
        
        for (int li = 0; li < terms.size(); li++) {
            for (int ri = li + 1; ri < terms.size(); ri++) {
                Formula left = notNull(terms.get(li)); 
                Formula right = notNull(terms.get(ri)); 
                
                if (left.equals(right)) {
                    // Idempotence: A ^ A -> A
                    // remove right term and continue
                    terms.remove(ri);
                    ri--;
                } else if (left instanceof Negation && ((Negation) left).getFormula().equals(right)) {
                    // Complementation: !A ^ A -> false
                    // whole conjunction becomes false
                    return False.INSTANCE;
                } else if (right instanceof Negation && ((Negation) right).getFormula().equals(left)) {
                    // Complementation: A ^ !A -> false
                    // whole conjunction becomes false
                    return False.INSTANCE;
                } else if (left instanceof Variable && right instanceof Disjunction
                            && isAndAbsorption((Variable) left, (Disjunction) right)) {
                    // Classical Absorption: A ^ (A v B) -> A
                    // remove right term and continue
                    terms.remove(ri);
                    ri--;
                } else if (right instanceof Variable && left instanceof Disjunction
                        && isAndAbsorption((Variable) right, (Disjunction) left)) {
                    // Classical Absorption: (A v B) ^ A -> A
                    // remove left term and continue
                    terms.remove(li);
                    li--;
                    break; // break inner loop, since we modified li
                } else if (left instanceof Negation && right instanceof Negation) {
                    Formula nestedLeft = ((Negation) left).getFormula();
                    Formula nestedRight = ((Negation) right).getFormula();
                    
                    if (nestedLeft instanceof Variable && nestedRight instanceof Conjunction) {
                        // Check for combination of complementation, De Morgan, Identity
                        Formula tmp = negatedAndComplementation((Negation) left, (Conjunction) nestedRight);
                        
                        if (tmp != null) {
                            // tmp replaces ri
                            terms.set(ri, tmp);
                            li = -1; // restart
                            break;
                        }
                        
                    } else if (nestedRight instanceof Variable && nestedLeft instanceof Conjunction) {
                        // Check for combination of complementation, De Morgan, Identity
                        Formula tmp = negatedAndComplementation((Negation) right, (Conjunction) nestedLeft);
                        
                        if (tmp != null) {
                            // tmp replaces li
                            terms.set(li, tmp);
                            li = -1; // restart
                            break;
                        }
                        
                    }
                }
                
            }
        }
        
        Formula result = notNull(terms.get(0));
        for (int i = 1; i < terms.size(); i++) {
            result = new Conjunction(result, notNull(terms.get(i)));
        }
        
        return result;
    }

    /**
     * Checks and resolves a negated AND complementation, identity, De Morgan.
     * Does the following transformations:<br/>
     * <pre><code>   !A &and; !(!A &and; B)
     * &rarr; !A &and; (A &or; !B)          | De Morgan, Double Negation
     * &rarr; (!A &and; A) &or; (!A &and; !B)   | Distribution
     * &rarr; (!A &and; !B)              | Complementation, Identity &or;</code></pre>
     *
     * @param negatedVar The negated variable (<tt>!A</tt> in the example).
     * @param negatedConjunction The conjunction to test (<tt>!(!A &and; B)</tt> in the example).
     * 
     * @return The term that replaces the {@link Conjunction} (!B in the example).
     */
    private Formula negatedAndComplementation(@NonNull Negation negatedVar, @NonNull Conjunction negatedConjunction) {
        
        Formula result = null;
        Formula innerLeft = negatedConjunction.getLeft();
        Formula innerRight = negatedConjunction.getRight();
        
        if (negatedVar.equals(innerLeft)) {
            Formula right = innerRight instanceof Negation ? ((Negation) innerRight).getFormula()
                : new Negation(innerRight);
            result = right;
        } else if (negatedVar.equals(innerRight)) {
            Formula right = innerLeft instanceof Negation ? ((Negation) innerLeft).getFormula()
                    : new Negation(innerLeft);
            result = right;
        }
        
        if (null != result) {
            // Try to simplify the new result
            result = result.accept(this);
        }
        
        return result;
    }

    /**
     * Recursive test if a conjunction fulfills the absorption rule.
     * @param var The variable of a conjunction.
     * @param disjunction The disjunction of a conjunction (will be recursively traversed if a compound disjunction).
     * @return <tt>true</tt> if it fulfills the absorption rule, i.e., if it can be simplified, <tt>false</tt>
     *     otherwise.
     */
    private boolean isAndAbsorption(Variable var, Disjunction disjunction) {
        Formula left = disjunction.getLeft();
        Formula right = disjunction.getRight();
        
        boolean result = var.equals(left) || var.equals(right);
        
        // Recursive part
        if (!result) {
            if (left instanceof Disjunction) {
                result = isAndAbsorption(var, (Disjunction) left);
            }
        }
        if (!result) {
            if (right instanceof Disjunction) {
                result = isAndAbsorption(var, (Disjunction) right);
            }
        }
        
        return result;
    }
    
    /**
     * Checks and resolves a negated OR complementation, identity, De Morgan.
     * Does the following transformations:<br/>
     * <pre><code>   !A &or; !(!A &or; B)
     * &rarr; !A &or; (A &and; !B)          | De Morgan, Double Negation
     * &rarr; (!A &or; A) &and; (!A &or; !B)   | Distribution
     * &rarr; (!A &or; !B)              | Complementation, Identity &or;</code></pre>
     *
     * @param negatedVar The negated variable (<tt>!A</tt> in the example).
     * @param negatedDisjunction The disjunction to test (<tt>(A &or; B)</tt> in the example).
     * @return The term that replaces the {@link Disjunction} (!B in the example).
     */
    private Formula negatedOrComplementation(@NonNull Negation negatedVar, @NonNull Disjunction negatedDisjunction) {
        
        Formula result = null;
        Formula innerLeft = negatedDisjunction.getLeft();
        Formula innerRight = negatedDisjunction.getRight();
        
        if (negatedVar.equals(innerLeft)) {
            Formula right = innerRight instanceof Negation ? ((Negation) innerRight).getFormula()
                : new Negation(innerRight);
            result = right;
        } else if (negatedVar.equals(innerRight)) {
            Formula right = innerLeft instanceof Negation ? ((Negation) innerLeft).getFormula()
                    : new Negation(innerLeft);
            result = right;
        }
        
        if (null != result) {
            // Try to simplify the new result
            result = result.accept(this);
        }
        
        return result;
    }
    
    /**
     * Recursive test if a disjunction fulfills the absorption rule.
     * @param var The variable of a disjunction.
     * @param conjunction The conjunction of a disjunction (will be recursively traversed if a compound conjunction).
     * @return <tt>true</tt> if it fulfills the absorption rule, i.e., if it can be simplified, <tt>false</tt>
     *     otherwise.
     */
    private boolean isOrAbsorption(Variable var, Conjunction conjunction) {
        Formula left = conjunction.getLeft();
        Formula right = conjunction.getRight();
        
        boolean result = var.equals(left) || var.equals(right);
        
        // Recursive part
        if (!result) {
            if (left instanceof Conjunction) {
                result = isOrAbsorption(var, (Conjunction) left);
            }
        }
        if (!result) {
            if (right instanceof Conjunction) {
                result = isOrAbsorption(var, (Conjunction) right);
            }
        }
        
        return result;
    }
    
}
