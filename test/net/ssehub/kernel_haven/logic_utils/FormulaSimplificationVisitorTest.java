package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Tests the {@link FormulaSimplificationVisitor}.
 * @author El-Sharkawy
 *
 */
@RunWith(Parameterized.class)
public class FormulaSimplificationVisitorTest {
    
    private Formula expected;
    private Formula inputFormula;

    /**
     * Sole constructor for the parameterized test case.
     * @param inputFormula The formula to be simplified.
     * @param expected The expected result.
     * @param testName The name of the test case.
     */
    public FormulaSimplificationVisitorTest(Formula inputFormula, Formula expected, String testName) {
        this.expected = expected;
        this.inputFormula = inputFormula;
    }

    /**
     * Creates the parameters for this test.
     * 
     * @return The parameters of this test.
     */
    @Parameters(name = "{2}: {0} -> {1}")
    public static Collection<Object[]> getParameters() {
        Variable varA = new Variable("A");
        return Arrays.asList(new Object[][] {
            // Conjunctions
            {and("A", "B"), and("A", "B"), "AND"},
            {and("A", "A"), varA, "Idempotence \u2227"},
            {and("A", True.INSTANCE), varA, "Identity \u2227"},
            {and(True.INSTANCE, "A"), varA, "Identity \u2227"},
            {and("A", False.INSTANCE), False.INSTANCE, "Annihilator \u2227"},
            {and(False.INSTANCE, "A"), False.INSTANCE, "Annihilator \u2227"},
            {and("A", not("A")), False.INSTANCE, "Complementation \u2227"},
            {and(not("A"), "A"), False.INSTANCE, "Complementation \u2227"},
            {and("A", or("A", "B")), varA, "Absorption \u2227"},
            {and(or("A", "B"), "A"), varA, "Absorption \u2227"},
            {and("A", and(or("A", "B"), "C")), and("A", "C"), "Nested Absorption \u2227"},
            {and(and(or("A", "B"), "C"), "A"), and("A", "C"), "Nested Absorption \u2227"},
            {and(not(not("A")), "B"), and("A", "B"), "Rewrite Simplified \u2227"},
            {and("B", not(not("A"))), and("B", "A"), "Rewrite Simplified \u2227"},
            
            // Disjunctions
            {or("A", "B"), or("A", "B"), "OR"},
            {or("A", "A"), varA, "Idempotence \u2228"},
            {or("A", False.INSTANCE), varA, "Identity \u2228"},
            {or(False.INSTANCE, "A"), varA, "Identity \u2228"},
            {or("A", True.INSTANCE), True.INSTANCE, "Annihilator \u2228"},
            {or(True.INSTANCE, "A"), True.INSTANCE, "Annihilator \u2228"},
            {or("A", not("A")), True.INSTANCE, "Complementation \u2228"},
            {or(not("A"), "A"), True.INSTANCE, "Complementation \u2228"},
            {or("A", and("A", "B")), varA, "Absorption \u2228"},
            {or(and("A", "B"), "A"), varA, "Absorption \u2228"},
            {or("A", or(and("A", "B"), "C")), or("A", "C"), "Nested Absorption \u2228"},
            {or(or(and("A", "B"), "C"), "A"), or("A", "C"), "Nested Absorption \u2228"},
            {or(not(not("A")), "B"), or("A", "B"), "Rewrite Simplified \u2228"},
            {or("B", not(not("A"))), or("B", "A"), "Rewrite Simplified \u2228"},
            
            // Negations
            {not("A"), not("A"), "Negation"},
            {not(not("A")), varA, "Double negation"},
            {not(True.INSTANCE), False.INSTANCE, "Negation on Constant"},
            {not(False.INSTANCE), True.INSTANCE, "Negation on Constant"},
            
            // Combinations
            {and(not("A"), not(and(not("A"), "B"))), and(not("A"), not("B")), "Negation/Absorption \u2227"},
            {and(not(and(not("A"), "B")), not("A")), and(not("A"), not("B")), "Negation/Absorption \u2227"},
            
            // Complex / scenario tests
            {or("A", or(and("C", or("D", "E")), and("A", "B"))), or(and("C", or("D", "E")), "A"), "Complex Absorbtion"},
            {or(or(or("D", "E"), "F"), "D"), or(or(or("D", "E"), "F"), "D"), "Unbalanced OR-Tree"},
            {and(or(and("A", "B"), "C"), or(or("D", "E"), or("F", "D"))),
                and(or(and("A", "B"), "C"), or(or("D", "E"), or("F", "D"))), "Complex keeps same"},
            
        });
    }
    
    /**
     * The parameterized test method.
     */
    @Test
    public void test() {
        FormulaSimplificationVisitor simplifier = new FormulaSimplificationVisitor();
        Formula simplified = inputFormula.accept(simplifier);
        
        Assert.assertEquals(expected, simplified);
    }
}
