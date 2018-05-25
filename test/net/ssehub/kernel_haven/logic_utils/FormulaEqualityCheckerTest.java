package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.cnf.ConverterException;
import net.ssehub.kernel_haven.cnf.SolverException;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Tests the {@link FormulaEqualityChecker}.
 * 
 * @author Adam
 */
public class FormulaEqualityCheckerTest {
    
    /**
     * Tests (un-)equality of simple variables.
     * 
     * @throws ConverterException unwanted.
     * @throws SolverException unwanted.
     */
    @Test
    public void testSimpleVariables() throws SolverException, ConverterException {
        FormulaEqualityChecker checker = new FormulaEqualityChecker();
        
        assertThat(checker.isLogicallyEqual(new Variable("A"), new Variable("A")), is(true));
        
        assertThat(checker.isLogicallyEqual(new Variable("A"), new Variable("B")), is(false));
    }
    
    /**
     * Tests (un-)equality of simple negated variables.
     * 
     * @throws ConverterException unwanted.
     * @throws SolverException unwanted.
     */
    @Test
    public void testSimpleNegation() throws SolverException, ConverterException {
        FormulaEqualityChecker checker = new FormulaEqualityChecker();
        
        assertThat(checker.isLogicallyEqual(not("A"), not("A")), is(true));
        
        assertThat(checker.isLogicallyEqual(not("A"), not("B")), is(false));
        assertThat(checker.isLogicallyEqual(not("A"), new Variable("A")), is(false));
    }
    
    /**
     * Tests (un-)equality of simple conjunctions.
     * 
     * @throws ConverterException unwanted.
     * @throws SolverException unwanted.
     */
    @Test
    public void testSimpleConjunctions() throws SolverException, ConverterException {
        FormulaEqualityChecker checker = new FormulaEqualityChecker();
        
        assertThat(checker.isLogicallyEqual(and("A", "B"), and("A", "B")), is(true));
        assertThat(checker.isLogicallyEqual(and("A", "B"), and("B", "A")), is(true));
        
        assertThat(checker.isLogicallyEqual(and("A", "B"), and("A", "A")), is(false));
        assertThat(checker.isLogicallyEqual(and("A", "B"), and("A", "C")), is(false));
        assertThat(checker.isLogicallyEqual(and("A", "B"), and("A", not("B"))), is(false));
        assertThat(checker.isLogicallyEqual(and("A", "B"), or("A", "B")), is(false));
    }
    
    /**
     * Tests (un-)equality of simple disjunctions.
     * 
     * @throws ConverterException unwanted.
     * @throws SolverException unwanted.
     */
    @Test
    public void testSimpleDisjunctions() throws SolverException, ConverterException {
        FormulaEqualityChecker checker = new FormulaEqualityChecker();
        
        assertThat(checker.isLogicallyEqual(or("A", "B"), or("A", "B")), is(true));
        assertThat(checker.isLogicallyEqual(or("A", "B"), or("B", "A")), is(true));
        
        assertThat(checker.isLogicallyEqual(or("A", "B"), or("A", "A")), is(false));
        assertThat(checker.isLogicallyEqual(or("A", "B"), or("A", "C")), is(false));
        assertThat(checker.isLogicallyEqual(or("A", "B"), or("A", not("B"))), is(false));
        assertThat(checker.isLogicallyEqual(or("A", "B"), and("A", "B")), is(false));
    }
    
    /**
     * Tests equality of a formula with its DNF representation.
     * 
     * @throws ConverterException unwanted.
     * @throws SolverException unwanted.
     */
    @Test
    public void testDnf() throws SolverException, ConverterException {
        FormulaEqualityChecker checker = new FormulaEqualityChecker();
        
        
        /*
         *  A | B | C | (A && B) || !C
         * ---+---+---+---------------
         *  0 | 0 | 0 |       1
         *  0 | 0 | 1 |       0
         *  0 | 1 | 0 |       1
         *  0 | 1 | 1 |       0
         *  1 | 0 | 0 |       1
         *  1 | 0 | 1 |       0
         *  1 | 1 | 0 |       1
         *  1 | 1 | 1 |       1
         */
        
        Formula f = or(and("A", "B"), not("C"));
        
        Formula dnf =
            or(
                and(and(not("A"), not("B")), not("C")),
            or(
                and(and(not("A"), "B"), not("C")),
            or(
                and(and("A", not("B")), not("C")),
            or(
                and(and("A", "B"), not("C")),
                and(and("A", "B"), "C")
            ))));
        
        assertThat(checker.isLogicallyEqual(f, dnf), is(true));
    }
    
    /**
     * Tests equality of formulas, with one containing an irrelevant variable.
     * 
     * @throws ConverterException unwanted.
     * @throws SolverException unwanted.
     */
    @Test
    public void testIrrelevantVariable() throws SolverException, ConverterException {
        FormulaEqualityChecker checker = new FormulaEqualityChecker();
        
        /*
         *  A | B | C | (A || B) || (!C && A) | (A || B)
         * ---+---+---+-----------------------+----------
         *  0 | 0 | 0 |       0               | 0
         *  0 | 0 | 1 |       0               | 0
         *  0 | 1 | 0 |       1               | 1
         *  0 | 1 | 1 |       1               | 1
         *  1 | 0 | 0 |       1               | 1
         *  1 | 0 | 1 |       1               | 1
         *  1 | 1 | 0 |       1               | 1
         *  1 | 1 | 1 |       1               | 1
         */
        
        assertThat(checker.isLogicallyEqual(or(or("A", "B"), and(not("C"), "A")),
                or("A", "B")), is(true));
    }

    // TODO: more test cases
    
}
