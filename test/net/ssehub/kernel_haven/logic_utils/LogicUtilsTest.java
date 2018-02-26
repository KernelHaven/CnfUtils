package net.ssehub.kernel_haven.logic_utils;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
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
        Variable varA = new Variable("A");
        Variable varB = new Variable("B");
        Formula complicated = new Conjunction(varA, varB);
        Formula simplified = LogicUtils.simplify(complicated);
        
        Assert.assertSame(complicated, simplified);
    }
    
    /**
     * Tests correct behavior if expression can not be simplified (OR).
     */
    @Test
    public void testNoSimplificationOR() {
        Variable varA = new Variable("A");
        Variable varB = new Variable("B");
        Formula complicated = new Disjunction(varA, varB);
        Formula simplified = LogicUtils.simplify(complicated);
        
        Assert.assertSame(complicated, simplified);
    }
    
    /**
     * Tests if <tt>A OR A</tt> can be simplified correctly.
     */
    @Test
    public void testORSimplification() {
        Variable varA = new Variable("A");
        Formula complicated = new Disjunction(varA, varA);
        Formula simplified = LogicUtils.simplify(complicated);
        
        Assert.assertSame(varA, simplified);
    }
    
    /**
     * Tests if <tt>A AND A</tt> can be simplified correctly.
     */
    @Test
    public void testANDSimplification() {
        Variable varA = new Variable("A");
        Formula complicated = new Conjunction(varA, varA);
        Formula simplified = LogicUtils.simplify(complicated);
        
        Assert.assertSame(varA, simplified);
    }
    
    /**
     * Tests if doubled negated formulas can be simplified correctly.
     */
    @Test
    public void testDoubleNotSimplification() {
        Variable varA = new Variable("A");
        Formula complicated = new Negation(new Negation(varA));
        Formula simplified = LogicUtils.simplify(complicated);
        
        Assert.assertSame(varA, simplified);
    }
    
    /**
     * Tests that the simplification for the formula A || (some complicated middle part) || (A && B) correctly drops
     * the last part.
     */
    @Test
    public void testComplicatedMiddlePart() {
        Variable varA = new Variable("A");
        Variable varB = new Variable("B");
        Variable varC = new Variable("C");
        Variable varD = new Variable("D");
        Variable varE = new Variable("E");
        
        Formula middlePart = new Conjunction(varC, new Disjunction(varD, varE));
        Formula rightPart = new Conjunction(varA, varB);
        
        Formula complicated = new Disjunction(varA, new Disjunction(middlePart, rightPart));
        Formula simplified = LogicUtils.simplify(complicated);
        
        assertEquals(new Disjunction(varA, middlePart), simplified);
    }
    
}
