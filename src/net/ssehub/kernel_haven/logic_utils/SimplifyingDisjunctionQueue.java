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
import net.ssehub.kernel_haven.util.logic.DepthCalculator;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.DisjunctionQueue;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * A {@link DisjunctionQueue} that uses a {@link SatSolver} to keep resulting {@link Formula}s small.
 * 
 * @author Adam
 */
public class SimplifyingDisjunctionQueue extends DisjunctionQueue {

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
                RelevancyType relevancy;
                try {
                    relevancy = checkRelevancy(previous, current);
                    
                } catch (StackOverflowError e) {
                    LOGGER.logWarning(varName + " has a PC that is too deep:",
                            "Current: " + new DepthCalculator().visit(current),
                            "Previous: " + new DepthCalculator().visit(previous));
                    
                    // consider both, to be safe
                    relevancy = RelevancyType.BOTH_RELEVANT;
                    
                } catch (ConverterException | SolverException e) {
                    if (null != varName) {
                        LOGGER.logExceptionWarning("Error while creating disjunction for conditions of " + varName, e);
                    } else {
                        LOGGER.logExceptionWarning("Error while creating disjunction", e);
                    }
                    
                    // consider both, to be safe
                    relevancy = RelevancyType.BOTH_RELEVANT;
                }
                    
                switch (relevancy) {
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
            }
            
            result = helperQueue.getDisjunction(varName);
        }
        
        // Reset
        reset();
        
        return result;
    }
    
    /**
     * Which formulas are relevant.
     */
    private static enum RelevancyType {
        
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
    private @NonNull RelevancyType checkRelevancy(@NonNull Formula previous, @NonNull Formula current)
            throws SolverException, ConverterException {
        
        RelevancyType result;
        
        // two sat() calls to check if previous, current or both need to be considered
        
        // sat(!previous AND current)
        if (solver.isSatisfiable(converter.convert(and(not(previous), current)))) {
            // true -> current is not subset of previous
            
            // sat(previous AND !current)
            if (solver.isSatisfiable(converter.convert(and(previous, not(current))))) {
                // neither previous nor current are subsets of each other -> consider both
                result = RelevancyType.BOTH_RELEVANT;
                
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
    
}
