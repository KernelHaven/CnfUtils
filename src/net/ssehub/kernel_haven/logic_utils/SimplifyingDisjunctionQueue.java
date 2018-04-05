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

    /**
     * Creates a new {@link SimplifyingDisjunctionQueue}.
     */
    public SimplifyingDisjunctionQueue() {
        super(true);
    }

    @Override
    public @NonNull Formula getDisjunction(@Nullable String varName) {
        Formula result;
        
        // Create disjunction of all elements
        if (isTrue || queue.isEmpty()) {
            result = True.INSTANCE;
        } else {
            
            
            SatSolver solver = new SatSolver();
            IFormulaToCnfConverter converter = FormulaToCnfConverterFactory.create(Strategy.RECURISVE_REPLACING);
            
            // all previously considered formulas ORd together (used in sat calls). 
            Formula previous = notNull(queue.poll());
            
            // a helper queue to create a balanced result; all considered formulas are added to it
            DisjunctionQueue helperQueue = new DisjunctionQueue(false);
            helperQueue.add(previous);
            
            for (Formula current : queue) {
                try {
                    
                    // two sat() calls to check if previous, current or both need to be considered
                    
                    // sat(!previous AND current)
                    // false -> current is subset of previous -> ignore current (nothing to do)
                    if (solver.isSatisfiable(converter.convert(and(not(previous), current)))) {
                        // true -> current is not subset of previous
                        
                        // sat(previous AND !current)
                        if (solver.isSatisfiable(converter.convert(and(previous, not(current))))) {
                            // neither previous nor current are subsets of each other -> consider both (add current)
                            previous = new Disjunction(previous, current);
                            helperQueue.add(current);
                            
                        } else {
                            // false -> previous is subset of current -> ignore previous (consider only current)
                            previous = current;
                            helperQueue.reset();
                            helperQueue.add(current);
                        }
                        
                    }
                    
                    previous = new Disjunction(previous, current);
                    
                } catch (ConverterException | SolverException e) {
                    if (null != varName) {
                        LOGGER.logExceptionWarning("Error while creating disjunction for conditions of " + varName, e);
                    } else {
                        LOGGER.logExceptionWarning("Error while creating disjunction", e);
                    }
                    
                    // add current to be safe
                    previous = new Disjunction(previous, current);
                    helperQueue.add(current);
                }
            }
            
            result = helperQueue.getDisjunction(varName);
            
        }
        
        // Reset
        reset();
        
        return result;
    }
    
}
