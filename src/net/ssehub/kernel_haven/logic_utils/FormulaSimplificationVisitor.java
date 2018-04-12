package net.ssehub.kernel_haven.logic_utils;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.IFormulaVisitor;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.NullHelpers;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Creates a more concise formula based on the visited input formula.
 * If the formula cannot be simplified, the same instance is returned.
 * @author El-Sharkawy
 *
 */
public class FormulaSimplificationVisitor implements IFormulaVisitor<Formula> {

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
        Formula inner = formula.getFormula();
        Formula result = inner.accept(this);
        
        if (result instanceof Negation) {
            // Double negation
            result = ((Negation) result).getFormula();
        } else if (result instanceof True) {
            result = False.INSTANCE;
        } else if (result instanceof False) {
            result = True.INSTANCE;
        } else {
            result = formula;
        }
        
        return result;
    }

    @Override
    public Formula visitDisjunction(@NonNull Disjunction formula) {
        Formula left = NullHelpers.notNull(formula.getLeft().accept(this));
        Formula right = NullHelpers.notNull(formula.getRight().accept(this));
        
        Formula result = null;
        if (left.equals(right)) {
            // Idempotence: A v A -> A
            result = left;
        } else if (left instanceof False) {
            // Identity: false v A -> A
            result = right;
        } else if (right instanceof False) {
            // Identity: A v false -> A
            result = left;
        } else if (left instanceof True) {
            // Annihilator: true v A -> true
            result = left;
        } else if (right instanceof True) {
            // Annihilator: A v true -> true
            result = right;
        } else if (left instanceof Negation && ((Negation) left).getFormula().equals(right)) {
            // Complementation: !A v A -> true
            result = True.INSTANCE;
        } else if (right instanceof Negation && ((Negation) right).getFormula().equals(left)) {
            // Complementation: A v !A -> true
            result = True.INSTANCE;
        } else if (left instanceof Variable) {
            // Test for Absorption
            if (right instanceof Conjunction) {
                // Classical Absorption: A v (A ^ B) -> A
                if (isOrAbsorbtion((Variable) left, (Conjunction) right)) {
                    result = left;
                }
            } else if (right instanceof Disjunction) {
                // Nested Absorption possible: A v (C v (A ^ B)) -> A v C
                result = nestedOrAbsorbtion((Variable) left, (Disjunction) right);
            }
        } else if (right instanceof Variable) {
            // Test for Absorption
            if (left instanceof Conjunction) {
                //Classical Absorption: (A ^ B) v A -> A
                if (isOrAbsorbtion((Variable) right, (Conjunction) left)) {
                    result = right;
                }
            } else if (left instanceof Disjunction) {
                // Nested Absorption possible: (C v (A ^ B)) v A -> A v C
                result = nestedOrAbsorbtion((Variable) right, (Disjunction) left);
            }
        }
        
        if (null == result) {
            if (formula.getLeft().equals(left) && formula.getRight().equals(right)) {
                // There was no simplification, keep old formula.
                result = formula;
            } else {
                // There was some (unknown) simplification, create new disjunction
                result = new Disjunction(left, right);
            }
        }
        
        return result;
    }

    @Override
    public Formula visitConjunction(@NonNull Conjunction formula) {
        Formula left = NullHelpers.notNull(formula.getLeft().accept(this));
        Formula right = NullHelpers.notNull(formula.getRight().accept(this));
        
        Formula result = null;
        if (left.equals(right)) {
            // Idempotence: A ^ A -> A
            result = left;
        } else if (left instanceof True) {
            // Identity: true ^ A -> A
            result = right;
        } else if (right instanceof True) {
            // Identity: A ^ true -> A
            result = left;
        } else if (left instanceof False) {
            // Annihilator: false ^ A -> false
            result = left;
        } else if (right instanceof False) {
            // Annihilator: A ^ false -> false
            result = right;
        } else if (left instanceof Negation && ((Negation) left).getFormula().equals(right)) {
            // Complementation: !A ^ A -> false
            result = False.INSTANCE;
        } else if (right instanceof Negation && ((Negation) right).getFormula().equals(left)) {
            // Complementation: A ^ !A -> false
            result = False.INSTANCE;
            
        } else if (left instanceof Variable) {         
            // Test for Absorption
            if (right instanceof Disjunction) {
                // Classical Absorption: A ^ (A v B) -> A
                if (isAndAbsorption((Variable) left, (Disjunction) right)) {
                    result = left;
                }
            } else if (right instanceof Conjunction) {
                // Nested Absorption possible: A ^ (C ^ (A v B)) -> A ^ C
                result = nestedAndAbsorbtion((Variable) left, (Conjunction) right);
            }
        } else if (right instanceof Variable) {
            // Test for Absorption
            if (left instanceof Disjunction) {
                // Classical Absorption: (A v B) ^ A -> A
                if (isAndAbsorption((Variable) right, (Disjunction) left)) {
                    result = right;
                }
            } else if (left instanceof Conjunction) {
                // Nested Absorption possible: (C ^ (A v B)) ^ A -> A ^ C
                result = nestedAndAbsorbtion((Variable) right, (Conjunction) left);
            }
        }
        
        if (null == result) {
            if (formula.getLeft().equals(left) && formula.getRight().equals(right)) {
                // There was no simplification, keep old formula.
                result = formula;
            } else {
                // There was some (unknown) simplification, create new conjunction
                result = new Conjunction(left, right);
            }
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
     * Tests and simplifies a nested and-absorption inside a conjunction. Will simplify<br/>
     * <tt>A &and; (X &and; (A &or; B)</tt> to <tt>A &and; X</tt>
     * @param var The variable of a disjunction.
     * @param conjunction The conjunction to test (right side of the example).
     * @return The simplified structure or <tt>null</tt> if the absorption rule could not be found.
     */
    private @Nullable Formula nestedAndAbsorbtion(@NonNull Variable var, Conjunction conjunction) {
        Formula left = conjunction.getLeft();
        @NonNull Formula right = conjunction.getRight();
        
        Formula result = null;
        if (left instanceof Disjunction && isAndAbsorption(var, (Disjunction) left)) {
            left = var;
        } else if (right instanceof Disjunction && isAndAbsorption(var, (Disjunction) right)) {
            right = var;
        }
        
        // Recursive part
        if (left instanceof Disjunction) {
            Formula tmp = nestedAndAbsorbtion(var, (Conjunction) left);
            left = null != tmp ? tmp : left;
        }
        if (right instanceof Disjunction) {
            Formula tmp = nestedAndAbsorbtion(var, (Conjunction) right);
            right = null != tmp ? tmp : right;
        }
        
        // Return new (simplified) conjunction only if one of the sides has been changed
        if (!left.equals(conjunction.getLeft()) || !right.equals(conjunction.getRight())) {
            result = new Conjunction(left, right);
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
    private boolean isOrAbsorbtion(Variable var, Conjunction conjunction) {
        Formula left = conjunction.getLeft();
        Formula right = conjunction.getRight();
        
        boolean result = var.equals(left) || var.equals(right);
        
        // Recursive part
        if (!result) {
            if (left instanceof Conjunction) {
                result = isOrAbsorbtion(var, (Conjunction) left);
            }
        }
        if (!result) {
            if (right instanceof Conjunction) {
                result = isOrAbsorbtion(var, (Conjunction) right);
            }
        }
        
        return result;
    }
    
    /**
     * Tests and simplifies a nested or-absorption inside a disjunction. Will simplify<br/>
     * <tt>A &or; (X &or; (A &and; B)</tt> to <tt>A &or; X</tt>
     * @param var The variable of a disjunction.
     * @param disjunction The disjunction to test (right side of the example).
     * @return The simplified structure or <tt>null</tt> if the absorption rule could not be found.
     */
    private @Nullable Formula nestedOrAbsorbtion(@NonNull Variable var, Disjunction disjunction) {
        Formula left = disjunction.getLeft();
        @NonNull Formula right = disjunction.getRight();
        
        Formula result = null;
        if (left instanceof Conjunction && isOrAbsorbtion(var, (Conjunction) left)) {
            left = var;
        } else if (right instanceof Conjunction && isOrAbsorbtion(var, (Conjunction) right)) {
            right = var;
        }
        
        // Recursive part
        if (left instanceof Disjunction) {
            Formula tmp = nestedOrAbsorbtion(var, (Disjunction) left);
            left = null != tmp ? tmp : left;
        }
        if (right instanceof Disjunction) {
            Formula tmp = nestedOrAbsorbtion(var, (Disjunction) left);
            right = null != tmp ? tmp : right;
        }
        
        // Return new (simplified) disjunction only if one of the sides has been changed
        if (!left.equals(disjunction.getLeft()) || !right.equals(disjunction.getRight())) {
            result = new Disjunction(left, right);
            result = result.accept(this);
        }
        
        return result;
    }
}
