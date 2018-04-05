package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;

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
        
        assertThat(queue.getDisjunction(), is(or("VAR_A", "VAR_B")));
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
    
}
