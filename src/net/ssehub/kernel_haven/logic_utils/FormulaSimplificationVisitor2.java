package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.logic_utils.test.FormulaStructureChecker.getAllConjunctionTerms;
import static net.ssehub.kernel_haven.logic_utils.test.FormulaStructureChecker.getAllDisjunctionTerms;
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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


    // CHECKSTYLE:OFF // method too long
    @Override
    public Formula visitDisjunction(@NonNull Disjunction formula) {
    // CHECKSTYLE:ON
        List<Formula> terms = new ArrayList<>();
        
        AtomicBoolean containsTrue = new AtomicBoolean(false);
        getAllDisjunctionTerms(formula).stream()
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
                
                if (isSameVariable(left, right)) {
                    // Idempotence: A v A -> A
                    // remove right term and continue
                    terms.remove(ri);
                    ri--;
                    
                } else if (isNegation(left) && isSameVariable(((Negation) left).getFormula(), right)) {
                    // Complementation: !A v A -> true
                    // whole disjunction becomes true
                    return True.INSTANCE;
                    
                } else if (isNegation(right) && isSameVariable(((Negation) right).getFormula(), left)) {
                    // Complementation: A v !A -> true
                    // whole disjunction becomes true
                    return True.INSTANCE;
                    
                } else if (isOrAbsorption(left, right)) {
                    // Classical Absorption: A v (A ^ B) -> A
                    // remove right term and continue
                    terms.remove(ri);
                    ri--;
                    
                } else if (isOrAbsorption(right, left)) {
                    // Classical Absorption: (A ^ B) v A -> A
                    // remove left term and continue
                    terms.remove(li);
                    li--;
                    break; // break inner loop, since we modified li
                    
                } else if (isNegatedOrAbsorption(left, right)) {
                    // Negated Absorption: !A v (A ^ B) -> !A v B
                    // replace right term
                    Formula replacement = getLeftOverNegatedOrAbsorption(getNegatedVariable(left), (Conjunction) right);
                    terms.set(ri, replacement);
                    li = -1; // restart
                    break;
                    
                } else if (isNegatedOrAbsorption(right, left)) {
                    // Negated Absorption: (A ^ B) v !A -> B v !A
                    // replace left term
                    Formula replacement = getLeftOverNegatedOrAbsorption(getNegatedVariable(right), (Conjunction) left);
                    terms.set(li, replacement);
                    li = -1; // restart
                    break;
                    
                }
            }
        }
        
        // use newTerms from now on, so that terms is "effectively final" and can be used in lambda above
        List<Formula> newTerms = terms;
        
        // Factoring out: (A ^ B) v (A ^ C) -> A ^ (B v C)
        // 1) check if all terms are Conjunctions
        boolean allConjunctions = true;
        for (Formula term : newTerms) {
            allConjunctions &= term instanceof Conjunction;
            if (!allConjunctions) {
                break;
            }
        }
        Set<@NonNull Variable> factoredOutvars = null;
        if (allConjunctions) {
            // 2) find variables that appear in all of the terms
            factoredOutvars = findVarThatAppearsInAllConjunctions(newTerms);
            if (!factoredOutvars.isEmpty()) {
                // 3) remove the variables from all the given terms
                newTerms = removeFromAllConjunctions(newTerms, factoredOutvars);
            }
        }
        
        Formula result;
        if (newTerms.isEmpty()) {
            // special case: "factoring out" removed all terms completely; only factored out part remains
            // factoredOutvars is not null if all terms have been "factored out"
            Iterator<@NonNull Variable> it = notNull(factoredOutvars).iterator();
            result = notNull(it.next());
            while (it.hasNext()) {
                result = new Conjunction(result, it.next());
            }
        } else {
            // construct normal disjunction
            result = notNull(newTerms.get(0));
            for (int i = 1; i < newTerms.size(); i++) {
                result = new Disjunction(result, notNull(newTerms.get(i)));
            }
            
            if (factoredOutvars != null) {
                // 4) add factored-out part
                for (Variable var : factoredOutvars) {
                    result = new Conjunction(var, result);
                }
            }
        }
        
        return result;
    }

    // CHECKSTYLE:OFF // method too long
    @Override
    public Formula visitConjunction(@NonNull Conjunction formula) {
    // CHECKSTYLE:ON
        List<Formula> terms = new ArrayList<>();
        
        AtomicBoolean containsFalse = new AtomicBoolean(false);
        getAllConjunctionTerms(formula).stream()
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
                
                if (isSameVariable(left, right)) {
                    // Idempotence: A ^ A -> A
                    // remove right term and continue
                    terms.remove(ri);
                    ri--;
                    
                } else if (isNegation(left) && isSameVariable(((Negation) left).getFormula(), right)) {
                    // Complementation: !A ^ A -> false
                    // whole conjunction becomes false
                    return False.INSTANCE;
                    
                } else if (isNegation(right) && isSameVariable(((Negation) right).getFormula(), left)) {
                    // Complementation: A ^ !A -> false
                    // whole conjunction becomes false
                    return False.INSTANCE;
                    
                } else if (isAndAbsorption(left, right)) {
                    // Classical Absorption: A ^ (A v B) -> A
                    // remove right term and continue
                    terms.remove(ri);
                    ri--;
                    
                } else if (isAndAbsorption(right, left)) {
                    // Classical Absorption: (A v B) ^ A -> A
                    // remove left term and continue
                    terms.remove(li);
                    li--;
                    break; // break inner loop, since we modified li
                    
                } else if (isNegatedAndAbsorption(left, right)) {
                    // Negated Absorption: !A ^ (A v B) -> !A ^ B
                    // replace right term
                    Formula replacement
                        = getLeftOverNegatedAndAbsorption(getNegatedVariable(left), (Disjunction) right);
                    terms.set(ri, replacement);
                    li = -1; // restart
                    break;
                    
                } else if (isNegatedAndAbsorption(right, left)) {
                    // Negated Absorption: (A v B) ^ !A -> B ^ !A
                    // replace left term
                    Formula replacement
                        = getLeftOverNegatedAndAbsorption(getNegatedVariable(right), (Disjunction) left);
                    terms.set(li, replacement);
                    li = -1; // restart
                    break;
                }
                
            }
        }
        
        // use newTerms from now on, so that terms is "effectively final" and can be used in lambda above
        List<Formula> newTerms = terms;
        
        // Factoring out: (A v B) ^ (A v C) -> A v (B ^ C)
        // 1) check if all terms are Disjunctions
        boolean allDisjunctions = true;
        for (Formula term : newTerms) {
            allDisjunctions &= term instanceof Disjunction;
            if (!allDisjunctions) {
                break;
            }
        }
        Set<@NonNull Variable> factoredOutvars = null;
        if (allDisjunctions) {
            // 2) find variables that appear in all of the terms
            factoredOutvars = findVarThatAppearsInAllDisjunctions(newTerms);
            if (!factoredOutvars.isEmpty()) {
                // 3) remove the variables from all the given terms
                newTerms = removeFromAllDisjunctions(newTerms, factoredOutvars);
            }
        }
        
        Formula result;
        if (newTerms.isEmpty()) {
            // special case: "factoring out" removed all terms completely; only factored out part remains
            // factoredOutvars is not null if all terms have been "factored out"
            Iterator<@NonNull Variable> it = notNull(factoredOutvars).iterator();
            result = notNull(it.next());
            while (it.hasNext()) {
                result = new Disjunction(result, it.next());
            }
        } else {
            // construct normal conjunction
            result = notNull(newTerms.get(0));
            for (int i = 1; i < newTerms.size(); i++) {
                result = new Conjunction(result, notNull(newTerms.get(i)));
            }
            
            if (factoredOutvars != null) {
                // 4) add factored-out part
                for (Variable var : factoredOutvars) {
                    result = new Disjunction(var, result);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Finds a set of variables that appear in all of the given conjunctions.
     * 
     * @param terms A list of {@link Conjunction}s.
     * 
     * @return A set of variables that appear in all conjunctions; may be empty.
     */
    private @NonNull Set<@NonNull Variable> findVarThatAppearsInAllConjunctions(@NonNull List<Formula> terms) {
        Set<@NonNull Variable> result = null;
        
        for (Formula term : terms) {
            List<@NonNull Formula> subTerms = getAllConjunctionTerms((Conjunction) notNull(term));
            Set<@NonNull Variable> vars = new HashSet<>(subTerms.size());
            for (Formula subTerm : subTerms) {
                if (isVariable(subTerm)) {
                    vars.add((Variable) subTerm);
                }
            }
            
            if (result == null) {
                result = vars;
            } else {
                result.retainAll(vars);
            }
        }
        
        return notNull(result);
    }
    
    /**
     * Finds a set of variables that appear in all of the given disjunctions.
     * 
     * @param terms A list of {@link Disjunction}s.
     * 
     * @return A set of variables that appear in all disjunctions; may be empty.
     */
    private @NonNull Set<@NonNull Variable> findVarThatAppearsInAllDisjunctions(@NonNull List<Formula> terms) {
        Set<@NonNull Variable> result = null;
        
        for (Formula term : terms) {
            List<@NonNull Formula> subTerms = getAllDisjunctionTerms((Disjunction) notNull(term));
            Set<@NonNull Variable> vars = new HashSet<>(subTerms.size());
            for (Formula subTerm : subTerms) {
                if (isVariable(subTerm)) {
                    vars.add((Variable) subTerm);
                }
            }
            
            if (result == null) {
                result = vars;
            } else {
                result.retainAll(vars);
            }
        }
        
        return notNull(result);
    }
    
    /**
     * Constructs a new list of conjunctions, with all the given variables removed.
     * 
     * @param terms The list of conjunctions to remove the variables from.
     * @param vars The variables to remove from all conjunctions.
     * 
     * @return A new list of conjunctions, with all the variables removed.
     */
    private @NonNull List<Formula> removeFromAllConjunctions(@NonNull List<Formula> terms,
            @NonNull Set<@NonNull Variable> vars) {
        
        List<Formula> result = new ArrayList<>(terms.size());
        
        for (Formula term : terms) {
            List<@NonNull Formula> subTerms = getAllConjunctionTerms((Conjunction) notNull(term));
            List<@NonNull Formula> newSubTerms = new ArrayList<>(subTerms.size());
                    
            for (Formula subTerm : subTerms) {
                if (!isVariable(subTerm) || !vars.contains(subTerm)) {
                    newSubTerms.add(subTerm);
                }
            }
            
            if (!newSubTerms.isEmpty()) {
                Formula newSubTerm = notNull(newSubTerms.get(0));
                for (int i = 1; i < newSubTerms.size(); i++) {
                    newSubTerm = new Conjunction(newSubTerm, newSubTerms.get(i));
                }
                result.add(newSubTerm);
            }
        }
        
        return result;
    }
    
    /**
     * Constructs a new list of disjunctions, with all the given variables removed.
     * 
     * @param terms The list of disjunctions to remove the variables from.
     * @param vars The variables to remove from all disjunctions.
     * 
     * @return A new list of disjunctions, with all the variables removed.
     */
    private @NonNull List<Formula> removeFromAllDisjunctions(@NonNull List<Formula> terms,
            @NonNull Set<@NonNull Variable> vars) {
        
        List<Formula> result = new ArrayList<>(terms.size());
        
        for (Formula term : terms) {
            List<@NonNull Formula> subTerms = getAllDisjunctionTerms((Disjunction) notNull(term));
            List<@NonNull Formula> newSubTerms = new ArrayList<>(subTerms.size());
                    
            for (Formula subTerm : subTerms) {
                if (!isVariable(subTerm) || !vars.contains(subTerm)) {
                    newSubTerms.add(subTerm);
                }
            }
            
            if (!newSubTerms.isEmpty()) {
                Formula newSubTerm = notNull(newSubTerms.get(0));
                for (int i = 1; i < newSubTerms.size(); i++) {
                    newSubTerm = new Disjunction(newSubTerm, newSubTerms.get(i));
                }
                result.add(newSubTerm);
            }
        }
        
        return result;
    }

    /**
     * Checks if the absorption rule applies. A &or; (A &and; B) &rarr; A.
     * 
     * @param possibleVar The variable that absorbs the conjunction (A in the example).
     * @param possibleConjunction The conjunction that is absorbed ((A &and; B) in the example).
     * 
     * @return Whether the absorption rule applies to the given formulas.
     */
    private boolean isOrAbsorption(@NonNull Formula possibleVar, @NonNull Formula possibleConjunction) {
        boolean result = false;
        
        if (isVariable(possibleVar) && isConjunction(possibleConjunction)) {
            Variable var = (Variable) possibleVar;
            List<@NonNull Formula> terms = getAllConjunctionTerms((Conjunction) possibleConjunction);
            
            for (Formula term : terms) {
                if (isSameVariable(var, term)) {
                    result = true;
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Checks if the negated absorption rule applies. !A &or; (A &and; B) &rarr; !A &or; B.
     * 
     * @param possibleNegatedVar The negated variable that absorbs parts of the conjunction (!A in the example).
     * @param possibleConjunction The conjunction that is partly absorbed ((A &and; B) in the example).
     * 
     * @return Whether the negated absorption rule applies to the given formulas.
     */
    private boolean isNegatedOrAbsorption(@NonNull Formula possibleNegatedVar, @NonNull Formula possibleConjunction) {
        boolean result = false;
        
        if (isNegatedVariable(possibleNegatedVar) && isConjunction(possibleConjunction)) {
            Variable var = getNegatedVariable(possibleNegatedVar);
            List<@NonNull Formula> terms = getAllConjunctionTerms((Conjunction) possibleConjunction);
            
            for (Formula term : terms) {
                if (isSameVariable(var, term)) {
                    result = true;
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Returns the left-over conjunction that remains after the negated or absoprtion.
     * !A &or; (A &and; B) &rarr; !A &or; B.
     * 
     * @param var The variable that absorbs parts of the conjunction (A in the example).
     * @param conjunction The conjunction that is partly absorbed ((A &and; B) in the example).
     * 
     * @return The left-over conjunction. (B in the example).
     * 
     * @see #isNegatedOrAbsorption(Formula, Formula)
     */
    private @NonNull Formula getLeftOverNegatedOrAbsorption(@NonNull Variable var, @NonNull Conjunction conjunction) {
        List<@NonNull Formula> terms = getAllConjunctionTerms(conjunction);
        Formula result = null;
        
        for (Formula term : terms) {
            if (!isSameVariable(term, var)) {
                if (result == null) {
                    result = term;
                } else {
                    result = new Conjunction(result, term);
                }
            }
        }
        
        return notNull(result);
    }
    
    /**
     * Checks if the absorption rule applies. A &and; (A &or; B) &rarr; A.
     * 
     * @param possibleVar The variable that absorbs the disjunction (A in the example).
     * @param possibleDisjunction The disjunction that is absorbed ((A &or; B) in the example).
     * 
     * @return Whether the absorption rule applies to the given formulas.
     */
    private boolean isAndAbsorption(@NonNull Formula possibleVar, @NonNull Formula possibleDisjunction) {
        boolean result = false;
        
        if (isVariable(possibleVar) && isDisjunction(possibleDisjunction)) {
            Variable var = (Variable) possibleVar;
            List<@NonNull Formula> terms = getAllDisjunctionTerms((Disjunction) possibleDisjunction);
            
            for (Formula term : terms) {
                if (isSameVariable(var, term)) {
                    result = true;
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Checks if the negated absorption rule applies. !A &and; (A &or; B) &rarr; !A &and; B.
     * 
     * @param possibleNegatedVar The negated variable that absorbs parts of the disjunction (!A in the example).
     * @param possibleDisjunction The disjunction that is partly absorbed ((A &or; B) in the example).
     * 
     * @return Whether the negated absorption rule applies to the given formulas.
     */
    private boolean isNegatedAndAbsorption(@NonNull Formula possibleNegatedVar, @NonNull Formula possibleDisjunction) {
        boolean result = false;
        
        if (isNegatedVariable(possibleNegatedVar) && isDisjunction(possibleDisjunction)) {
            Variable var = getNegatedVariable(possibleNegatedVar);
            List<@NonNull Formula> terms = getAllDisjunctionTerms((Disjunction) possibleDisjunction);
            
            for (Formula term : terms) {
                if (isSameVariable(var, term)) {
                    result = true;
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Returns the left-over disjunction that remains after the negated and absoprtion.
     * !A &and; (A &or; B) &rarr; !A &and; B.
     * 
     * @param var The variable that absorbs parts of the disjunction (A in the example).
     * @param disjunction The disjunction that is partly absorbed ((A &or; B) in the example).
     * 
     * @return The left-over disjunction. (B in the example).
     * 
     * @see #isNegatedAndAbsorption(Formula, Formula)
     */
    private @NonNull Formula getLeftOverNegatedAndAbsorption(@NonNull Variable var, @NonNull Disjunction disjunction) {
        List<@NonNull Formula> terms = getAllDisjunctionTerms(disjunction);
        Formula result = null;
        
        for (Formula term : terms) {
            if (!isSameVariable(term, var)) {
                if (result == null) {
                    result = term;
                } else {
                    result = new Disjunction(result, term);
                }
            }
        }
        
        return notNull(result);
    }
    
    /**
     * Checks if the given formula is a conjunction.
     * 
     * @param formula The formula to check.
     * 
     * @return Whether the formula is a conjunction.
     */
    private static boolean isConjunction(@NonNull Formula formula) {
        return formula instanceof Conjunction;
    }
    
    /**
     * Checks if the given formula is a disjunction.
     * 
     * @param formula The formula to check.
     * 
     * @return Whether the formula is a disjunction.
     */
    private static boolean isDisjunction(@NonNull Formula formula) {
        return formula instanceof Disjunction;
    }
    
    /**
     * Checks if the given formula is a negation.
     * 
     * @param formula The formula to check.
     * 
     * @return Whether the formula is a negation.
     */
    private static boolean isNegation(@NonNull Formula formula) {
        return formula instanceof Negation;
    }
    
    /**
     * Checks if the given formula is a variable.
     * 
     * @param formula The formula to check.
     * 
     * @return Whether the formula is a variable.
     */
    private static boolean isVariable(@NonNull Formula formula) {
        return formula instanceof Variable;
    }
    
    /**
     * Checks if the given formula is a negated variable.
     * 
     * @param formula The formula to check.
     * 
     * @return Whether the formula is a negated variable.
     */
    private static boolean isNegatedVariable(@NonNull Formula formula) {
        return isNegation(formula) && isVariable(((Negation) formula).getFormula()); 
    }
    
    /**
     * Returns the variable that is nested inside the given negation.
     * 
     * @param formula A {@link Negation} that contains a {@link Variable}.
     * 
     * @return The variable that is inside the given negation.
     * 
     * @see #isNegatedVariable(Formula).
     */
    private static @NonNull Variable getNegatedVariable(@NonNull Formula formula) {
        return (Variable) ((Negation) formula).getFormula();
    }
    
    /**
     * Checks if the two given formulas are {@link Variable}s and equal.
     * 
     * @param f1 The first formula.
     * @param f2 The second formula.
     * 
     * @return Whether the two formulas are the same {@link Variable}.
     */
    private static boolean isSameVariable(@NonNull Formula f1, @NonNull Formula f2) {
        boolean result = false;
        
        if (isVariable(f1) && isVariable(f2)) {
            result = f1.equals(f2);
        }
        
        return result;
    }
    
}
