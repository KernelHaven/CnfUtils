package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link SimplifyingDisjunctionQueue}.
 * 
 * @author Adam
 */
public class SimplifyingDisjunctionQueueTest {

    /**
     * Tests adding two formulas that can't be simplified.
     */
    @Test
    public void testNotSimplyfiable() {
        SimplifyingDisjunctionQueue queue = new SimplifyingDisjunctionQueue();
        
        queue.add(new Variable("VAR_A"));
        queue.add(new Variable("VAR_B"));
        
        assertThat(queue.getDisjunction(), is(or("VAR_A", "VAR_B")));
    }

    /**
     * Tests a queue which contains a single true (thus the whole disjunction becomes true).
     */
    @Test
    public void testTrue() {
        SimplifyingDisjunctionQueue queue = new SimplifyingDisjunctionQueue();
        
        queue.add(new Variable("VAR_A"));
        queue.add(new Variable("VAR_B"));
        queue.add(True.INSTANCE);
        queue.add(new Variable("VAR_C"));
        queue.add(new Variable("VAR_D"));
        
        assertThat(queue.getDisjunction(), is(True.INSTANCE));
    }
    
    /**
     * Tests an empty queue (true).
     */
    @Test
    public void testEmpty() {
        SimplifyingDisjunctionQueue queue = new SimplifyingDisjunctionQueue();
        
        assertThat(queue.getDisjunction(), is(True.INSTANCE));
    }
    
    /**
     * Tests adding two formulas: the second is a subset of the first.
     */
    @Test
    public void testSecondIsSubsetOfFirst() {
        SimplifyingDisjunctionQueue queue = new SimplifyingDisjunctionQueue();
        
        queue.add(or("VAR_A", "VAR_B"));
        queue.add(new Variable("VAR_B"));
        
        assertThat(queue.getDisjunction(), is(or("VAR_A", "VAR_B")));
    }
    
    /**
     * Tests adding two formulas: the first is a subset of the second.
     */
    @Test
    public void testFirstIsSubsetOfSecond() {
        SimplifyingDisjunctionQueue queue = new SimplifyingDisjunctionQueue();
        
        queue.add(new Variable("VAR_B"));
        queue.add(or("VAR_A", "VAR_B"));
        
        assertDisjunction(or("VAR_A", "VAR_B"), queue.getDisjunction());
        //assertThat(queue.getDisjunction(), is(or("VAR_A", "VAR_B")));
    }
    
    /**
     * Tests adding two (logically) equal formulas.
     */
    @Test
    public void testEqual() {
        SimplifyingDisjunctionQueue queue = new SimplifyingDisjunctionQueue();
        
        queue.add(or("VAR_A", "VAR_B"));
        queue.add(or("VAR_B", "VAR_A"));
        
        assertThat(queue.getDisjunction(), is(or("VAR_A", "VAR_B")));
    }
    
    /**
     * Tests elimination of irrelevant sub-formulas if {@link SimplifyingDisjunctionQueue#USE_RECURSIVE_SPLIT}
     * is enabled.
     */
    @Test
    public void testSubFormulaEleminationForDisjunctions() {
        SimplifyingDisjunctionQueue queue = new SimplifyingDisjunctionQueue();
        
        @NonNull Formula input1 = or(or("VAR_A", "VAR_B"), "VAR_C");
        @NonNull Formula input2 = or(or("VAR_A", "VAR_B"), "VAR_D");
        queue.add(input1);
        queue.add(input2);
        
        Formula expected;
        if (SimplifyingDisjunctionQueue.USE_RECURSIVE_SPLIT) {
            // Expected: A or B or C or D
            expected = or(or("VAR_A", "VAR_B"), or("VAR_C", "VAR_D"));
        } else {
            // Expected: (A or B or C) or (A or B or D)
            expected = or(input1, input2);
        }
        
        assertDisjunction(expected, queue.getDisjunction());
    }
    
    /**
     * Tests elimination of irrelevant sub-formulas if {@link SimplifyingDisjunctionQueue#USE_RECURSIVE_SPLIT}
     * is enabled.
     */
    @Test
    public void testSubFormulaEleminationForConjunctions() {
        SimplifyingDisjunctionQueue queue = new SimplifyingDisjunctionQueue();
        
        @NonNull Formula input1 = or(or("VAR_A", "VAR_B"), "VAR_C");
        @NonNull Formula input2 = or(not(and(not("VAR_D"), not("VAR_B"))), "VAR_A");
        queue.add(input1);
        queue.add(input2);
        
        Formula expected;
        if (SimplifyingDisjunctionQueue.USE_RECURSIVE_SPLIT) {
            // Expected: A or B or C or D
            expected = or(or("VAR_A", "VAR_B"), or("VAR_C", "VAR_D"));
        } else {
            // Expected: (A or B or C) or (A or B or D)
            expected = or(input1, input2);
        }
        
        assertDisjunction(expected, queue.getDisjunction());
    }
    
    /**
     * Tests elimination of irrelevant sub-formulas if {@link SimplifyingDisjunctionQueue#USE_RECURSIVE_SPLIT}
     * is enabled, but if nothing can be optimized.
     */
    @Test
    public void testSubFormulaEleminationWithoutPotentialForImprovement() {
        SimplifyingDisjunctionQueue queue = new SimplifyingDisjunctionQueue();
        
        @NonNull Formula input1 = or(or("VAR_A", "VAR_B"), "VAR_C");
        @NonNull Formula input2 = or("VAR_D", "VAR_E");
        queue.add(input1);
        queue.add(input2);
        
        Formula expected = or(input1, input2);
//        if (SimplifyingDisjunctionQueue.USE_RECURSIVE_SPLIT) {
//            // Expected: A or B or C or D
//            expected = or(or(or("VAR_A", "VAR_B"), or("VAR_C", "VAR_D"), "VAR_E"));
//        } else {
//            // Expected: (A or B or C) or (A or B or D)
//            expected = or(input1, input2);
//        }
        
        assertDisjunction(expected, queue.getDisjunction());
    }
    
    /**
     * Helper to test if a disjunction is equivalent to another disjunction (splits the toplevel elements
     * to allow reordering).
     * @param expected The expected disjunction
     * @param actual The tested disjunction
     */
    private static void assertDisjunction(Formula expected, Formula actual) {
        String[] expectedElements = expected.toString().split("\\s*\\|\\|\\s*");
        String[] actualElements = actual.toString().split("\\s*\\|\\|\\s*");
        
        assertThat(Arrays.asList(actualElements), hasItems(expectedElements));
        Assert.assertEquals("Unexpected elements of literals/variables found:", expectedElements.length,
                actualElements.length);
    }
}
