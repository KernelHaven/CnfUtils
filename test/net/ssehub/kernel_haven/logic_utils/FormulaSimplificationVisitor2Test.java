package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.kernel_haven.cnf.ConverterException;
import net.ssehub.kernel_haven.cnf.SolverException;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link FormulaSimplificationVisitor2}.
 * 
 * @author El-Sharkawy
 * @author Adam
 */
@RunWith(Parameterized.class)
public class FormulaSimplificationVisitor2Test {
    
    private @NonNull Formula expected;
    private @NonNull Formula inputFormula;

    /**
     * Sole constructor for the parameterized test case.
     * @param inputFormula The formula to be simplified.
     * @param expected The expected result.
     * @param testName The name of the test case.
     */
    public FormulaSimplificationVisitor2Test(@NonNull Formula inputFormula, @NonNull Formula expected,
            @NonNull String testName) {
        
        this.expected = expected;
        this.inputFormula = inputFormula;
    }

    /**
     * Creates the parameters for this test.
     * 
     * @return The parameters of this test.
     */
    // CHECKSTYLE:OFF
    @Parameters(name = "{2}: {0} -> {1}")
    public static Collection<Object[]> getParameters() {
        Variable varA = new Variable("A");
        return Arrays.asList(new Object[][] {
            // Conjunctions
            {and("A", "B"), and("A", "B"), "\u2227"},
            {and(True.INSTANCE, True.INSTANCE), True.INSTANCE, "All True \u2227"},
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
            {and(not("A"), or("A", "B")), and(not("A"), "B"), "Negated Variable Absorption \u2227"},
            {and(or("A", "B"), not("A")), and("B", not("A")), "Negated Variable Absorption \u2227"},
            {and(not("A"), or("A", or("B", "C"))), and(not("A"), or("C", "B")), "Negated Variable Absorption Multiple \u2227"},
            {and(or("A", or("B", "C")), not("A")), and(or("C", "B"), not("A")), "Negated Variable Absorption Multiple \u2227"},
            {and(or("A", "B"), or("A", "C")), or("A", and("B", "C")), "Factoring out \u2227"},
            {and(or("A", or("B", "D")), or("A", "C")), or("A", and(or("D", "B"), "C")), "Factoring out larger \u2227"},
            {and(or("A", "C"), or("A", "C")), or("C", "A"), "Factoring out same \u2227"},
            {or(and("A", and("B", "C")), and("A", "B")), and("B", "A"), "Factoring out whole term \u2227"},
            
            {and(not("A"), not(and(not("C"), "B"))), and(not("A"), not(and(not("C"), "B"))), "No Rule Applies \u2227"},
            {and(not(and(not("C"), "B")), not("A")), and(not(and(not("C"), "B")), not("A")), "No Rule Applies \u2227"},
            {and(not(or(not("A"), "B")), not("A")), and(not(or(not("A"), "B")), not("A")), "No Rule Applies \u2227"},
            
            // Disjunctions
            {or("A", "B"), or("A", "B"), "\u2228"},
            {or(False.INSTANCE, False.INSTANCE), False.INSTANCE, "All False \u2228"},
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
            {or(not("A"), and("A", "B")), or(not("A"), "B"), "Negated Variable Absorption \u2228"},
            {or(and("A", "B"), not("A")), or("B", not("A")), "Negated Variable Absorption \u2228"},
            {or(not("A"), and("A", and("B", "C"))), or(not("A"), and("C", "B")), "Negated Variable Absorption Multiple \u2228"},
            {or(and("A", and("B", "C")), not("A")), or(and("C", "B"), not("A")), "Negated Variable Absorption Multiple \u2228"},
            {or(and("A", "B"), and("A", "C")), and("A", or("B", "C")), "Factoring out \u2228"},
            {or(and("A", and("B", "D")), and("A", "C")), and("A", or(and("D", "B"), "C")), "Factoring out larger \u2228"},
            {or(and("A", "C"), and("A", "C")), and("C", "A"), "Factoring out same \u2228"},
            {and(or("A", or("B", "C")), or("A", "B")), or("B", "A"), "Factoring out whole term \u2228"},
            
            {or(not("A"), not(or(not("C"), "B"))), or(not("A"), not(or(not("C"), "B"))), "No Rule Applies \u2228"},
            {or(not(or(not("C"), "B")), not("A")), or(not(or(not("C"), "B")), not("A")), "No Rule Applies \u2228"},
            {or(not(and(not("C"), "B")), not("A")), or(not(and(not("C"), "B")), not("A")), "No Rule Applies \u2228"},
            
            // Negations
            {not("A"), not("A"), "Negation"},
            {not(not("A")), varA, "Double negation"},
            {not(True.INSTANCE), False.INSTANCE, "Negation on Constant"},
            {not(False.INSTANCE), True.INSTANCE, "Negation on Constant"},
            {not(and("A", True.INSTANCE)), not("A"), "Simplification inside negation"},

            // Complex / scenario tests
            {or("A", or(and("C", or("D", "E")), and("A", "B"))), or("A", and("C", or("D", "E"))), "Complex Absorbtion"},
            {or(or(or("D", "E"), "F"), "D"), or(or("D", "F"), "E"), "Unbalanced OR-Tree"},
            {and(or(and("A", "B"), "C"), or(or("D", "E"), or("F", "D"))),  and(or(and("A", "B"), "C"), or(or("D", "E"), "F")), "Complex keeps (almost) same"},
        });
    }
    // CHECKSTYLE:ON
    
    /**
     * The parameterized test method.
     * 
     * @throws ConverterException If SAT-solving fails.
     * @throws SolverException If SAT-solving fails.
     */
    @Test
   public void test() throws SolverException, ConverterException {
        FormulaSimplificationVisitor2 simplifier = new FormulaSimplificationVisitor2();
        Formula simplified = inputFormula.accept(simplifier);
        
        Assert.assertEquals(expected, simplified);
        
        assertThat(new FormulaEqualityChecker().isLogicallyEqual(simplified, inputFormula), is(true));
    }
}
