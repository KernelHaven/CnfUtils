package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Tests the {@link LogicUtils}, which does not require a lot of tests, since it is basically only a wrapper for
 * a third party library, which is also tested.
 * However, there is still some stuff from us, which should be tested, e.g., correct translation.
 * @author El-Sharkawy
 *
 */
public class LogicUtilsTest {

    /**
     * Tests correct behavior if expression can not be simplified (AND).
     */
    @Test
    public void testNoSimplificationAND() {
        Formula complicated = and("A", "B");
        Formula simplified = LogicUtils.simplify(complicated);
        
        Assert.assertEquals(complicated, simplified);
    }
    
    /**
     * Tests correct behavior if expression can not be simplified (OR).
     */
    @Test
    public void testNoSimplificationOR() {
        Formula complicated = or("A", "B");
        Formula simplified = LogicUtils.simplify(complicated);
        
        Assert.assertEquals(complicated, simplified);
    }
    
    /**
     * Tests if <tt>A OR A</tt> can be simplified correctly.
     */
    @Test
    public void testORSimplification() {
        Variable varA = new Variable("A");
        Formula complicated = or("A", "A");
        Formula simplified = LogicUtils.simplify(complicated);
        
        Assert.assertEquals(varA, simplified);
    }
    
    /**
     * Tests if <tt>A AND A</tt> can be simplified correctly.
     */
    @Test
    public void testANDSimplification() {
        Variable varA = new Variable("A");
        Formula complicated = and("A", "A");
        Formula simplified = LogicUtils.simplify(complicated);
        
        Assert.assertEquals(varA, simplified);
    }
    
    /**
     * Tests if doubled negated formulas can be simplified correctly.
     */
    @Test
    public void testDoubleNotSimplification() {
        Variable varA = new Variable("A");
        Formula complicated = not(not("A"));
        Formula simplified = LogicUtils.simplify(complicated);
        
        Assert.assertEquals(varA, simplified);
    }
    
    /**
     * Tests that the simplification for the formula A || (some complicated middle part) || (A && B) correctly drops
     * the last part.
     */
    @Test
    public void testComplicatedMiddlePart() {
        Formula middlePart = and("C", or("D", "E"));
        
        Formula complicated = or("A", or(middlePart, and("A", "B")));
        Formula simplified = LogicUtils.simplify(complicated);
        
        assertEquals(or("A", middlePart), simplified);
    }
    
}
