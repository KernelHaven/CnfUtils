package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import net.ssehub.kernel_haven.cnf.ConverterException;
import net.ssehub.kernel_haven.cnf.FormulaToCnfConverterFactory;
import net.ssehub.kernel_haven.cnf.FormulaToCnfConverterFactory.Strategy;
import net.ssehub.kernel_haven.cnf.IFormulaToCnfConverter;
import net.ssehub.kernel_haven.cnf.SatSolver;
import net.ssehub.kernel_haven.cnf.SolverException;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.DisjunctionQueue;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * A {@link DisjunctionQueue} that uses a {@link SatSolver} to keep resulting {@link Formula}s small.
 * 
 * @author Adam
 */
public class SimplifyingDisjunctionQueue extends DisjunctionQueue {

    static final boolean USE_RECURSIVE_SPLIT = true;
    private @NonNull SatSolver solver;
    
    private @NonNull IFormulaToCnfConverter converter;
    
    /**
     * Creates a new {@link SimplifyingDisjunctionQueue}.
     */
    public SimplifyingDisjunctionQueue() {
        super(true);
        
        solver = new SatSolver();
        converter = FormulaToCnfConverterFactory.create(Strategy.RECURISVE_REPLACING);
    }

    @Override
    public @NonNull Formula getDisjunction(@Nullable String varName) {
        Formula result;
        
        // Create disjunction of all elements
        if (isTrue || queue.isEmpty()) {
            result = True.INSTANCE;
        } else {
            // all previously considered formulas ORd together (used in sat calls). 
            Formula previous = notNull(queue.poll());
            // a helper queue to create a balanced result; all considered formulas are added to it
            DisjunctionQueue helperQueue = new DisjunctionQueue(false);
            helperQueue.add(previous);
            
            for (Formula current : queue) {
                IRelevancyType relevancy;
                try {
                    relevancy = checkRelevancy(previous, current);
                    
                } catch (ConverterException | SolverException e) {
                    if (null != varName) {
                        LOGGER.logExceptionWarning("Error while creating disjunction for conditions of " + varName, e);
                    } else {
                        LOGGER.logExceptionWarning("Error while creating disjunction", e);
                    }
                    
                    // consider both, to be safe
                    relevancy = RelevancyType.BOTH_RELEVANT;
                }
                
                if (relevancy instanceof RelevancyType) {
                    switch ((RelevancyType) relevancy) {
                    case PREVIOUS_RELEVANT:
                        // consider only previous; current is ignored (do nothing)
                        break;
                        
                    case CURRENT_RELEVANT:
                        // consider only current; previous is overridden
                        previous = current;
                        helperQueue.reset();
                        helperQueue.add(current);
                        break;
                        
                    case BOTH_RELEVANT:
                        // add current
                        previous = new Disjunction(previous, current);
                        helperQueue.add(current);
                        break;
                        
                    default:
                        throw new RuntimeException("Invalid relevancy: " + relevancy); // can't happen
                    }
                } else if (relevancy instanceof SubRelevance) {
                    // add sub formula of current
                    SubRelevance subRelevance = (SubRelevance) relevancy;
                    Formula relevantPart = subRelevance.relevantSubFormula;
                    previous = new Disjunction(previous, relevantPart);
                    helperQueue.add(relevantPart);
                } else {
                    // can't happen
                    throw new RuntimeException("Invalid relevancy class: " + relevancy.getClass().getCanonicalName());
                }
            }
            
            result = helperQueue.getDisjunction(varName);
        }
        
        // Reset
        reset();
        return result;
    }
    
    /**
     * Markup interface.
     * @author El-Sharkawy
     *
     */
    private static interface IRelevancyType {
        
    }
    
    /**
     * Which formulas are relevant.
     */
    private static enum RelevancyType implements IRelevancyType {
        
        /**
         * Both, current and previous, need to be considered.
         */
        BOTH_RELEVANT,
        
        /**
         * Only previous needs to be considered; current is a subset of previous.
         */
        PREVIOUS_RELEVANT,
        
        /**
         * Only current needs to be considered; previous is a subset of current.
         */
        CURRENT_RELEVANT;
        
    }
    
    /**
     * Marks a sub formula to be relevant, while the eliminated formula is irrelevant.
     * @author El-Sharkawy
     *
     */
    private static class SubRelevance implements IRelevancyType {
        /**
         * The remaining relevant part of a formula.
         */
        private @NonNull Formula relevantSubFormula;
        
        /**
         * Sole constructor.
         * @param relevantSubFormula The relevant part of a bigger formula which shall be kept.
         */
        private SubRelevance(@NonNull Formula relevantSubFormula) {
            this.relevantSubFormula = relevantSubFormula;
        }
    }
    
    /**
     * Checks which of the given {@link Formula}s is relevant.
     * 
     * @param previous All previously relevant formulas, OR'd together.
     * @param current The new formula that will (possibly) be added.
     * 
     * @return Which formulas are relevant.
     * 
     * @throws ConverterException If CNF conversion fails.
     * @throws SolverException If the SAT solver fails. 
     */
    private @NonNull IRelevancyType checkRelevancy(@NonNull Formula previous, @NonNull Formula current)
            throws SolverException, ConverterException {
        
        IRelevancyType result = null;
        
        // two sat() calls to check if previous, current or both need to be considered
        
        // sat(!previous AND current)
        if (solver.isSatisfiable(converter.convert(and(not(previous), current)))) {
            // true -> current is not subset of previous
            
            // sat(previous AND !current)
            if (solver.isSatisfiable(converter.convert(and(previous, not(current))))) {
                // neither previous nor current are subsets of each other -> consider both
                // Check if a sub formula is covered by previous 
                result = USE_RECURSIVE_SPLIT ? recursiveRelevanceAnalysis(not(previous), current)
                    : RelevancyType.BOTH_RELEVANT;
                if (null == result) {
                    result = RelevancyType.BOTH_RELEVANT;
                }
                
            } else {
                // false -> previous is subset of current -> ignore previous (consider only current)
                result = RelevancyType.CURRENT_RELEVANT;
            }
            
        } else {
            // false -> current is subset of previous -> ignore current
            result = RelevancyType.PREVIOUS_RELEVANT;
        }
        
        
        return result;
    }

    /**
     * Controls the recursion of {@link #checkRelevancy(Formula, Formula)},
     * {@link #checkSubRelevancy(Formula, Formula, Formula)}. Maybe dis-/en-abled via {@link #USE_RECURSIVE_SPLIT}.
     * @param notPrevious All previously relevant formulas, OR'd together, won't be touched, required for sat checks
     *     (negated form, as only this is needed for SAT call, this should reduce unnecessary creation of objects).
     * @param current The current element, which will be recursively split into smaller pieces as long either
     *     a sub element is covered by previous or it cannot be cut into smaller pieces.
     * 
     * @return <tt>null</tt> if current does not contain any elements covered by previous or the remaining part which
     *     must be added to previous (which does not contain the irrelevant part anymore).
     * @throws ConverterException If CNF conversion fails.
     * @throws SolverException If the SAT solver fails. 
     */
    private @Nullable SubRelevance recursiveRelevanceAnalysis(@NonNull Formula notPrevious, @NonNull Formula current)
        throws SolverException, ConverterException {
        
        SubRelevance result = null;
        if (current instanceof Disjunction) {
            Formula left = ((Disjunction) current).getLeft();
            Formula right = ((Disjunction) current).getRight();
            
            result = checkSubRelevancy(notPrevious, left, right);
            if (null == result) {
                result = checkSubRelevancy(notPrevious, right, left);
            }
        } else if (current instanceof Negation && ((Negation) current).getFormula() instanceof Conjunction) {
            // Transform: !(A AND B) into !A OR !B
            Conjunction inner = (Conjunction) ((Negation) current).getFormula();
            Formula left = inner.getLeft();
            left = (left instanceof Negation) ? ((Negation) left).getFormula() : new Negation(left);
            Formula right = inner.getRight();
            right = (right instanceof Negation) ? ((Negation) right).getFormula() : new Negation(right);
            
            result = checkSubRelevancy(notPrevious, left, right);
            if (null == result) {
                result = checkSubRelevancy(notPrevious, right, left);
            }
        }
        return result;
    }
    
    /**
     * Checks which of the given {@link Formula}s is relevant. recursive function, which aborts after the first match.
     * 
     * @param notPrevious All previously relevant formulas, OR'd together (negated form, as only this is needed for SAT
     *     call, this should reduce unnecessary creation of objects).
     * @param current A sub element of the new formula that will (possibly) be added (will be disjunction-wise split).
     * @param base The other part of current, which must be added in case that current is irrelevant.
     * 
     * @return A relevant sub formula of current or <tt>null</tt> if the whole formula is relevant
     * 
     * @throws ConverterException If CNF conversion fails.
     * @throws SolverException If the SAT solver fails. 
     */
    private @Nullable SubRelevance checkSubRelevancy(@NonNull Formula notPrevious, @NonNull Formula current,
        @NonNull Formula base) throws SolverException, ConverterException {
        
        // Here we check only if the current formula (which covers a smaller configuration space) is covered by previous
        SubRelevance result = null;
        
        // sat(!previous AND current)
        if (solver.isSatisfiable(converter.convert(and(notPrevious, current)))) {
            // true -> current is not subset of previous: continue recursion if possible
            
            // Recursion
            result = recursiveRelevanceAnalysis(notPrevious, current);
        } else {
            // false -> current is subset of previous -> ignore current, keep base
            result = new SubRelevance(base);
        }
        
        return result;
    }
    
}
