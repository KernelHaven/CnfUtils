package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link FormulaStructureChecker#isStructurallyEqual(Formula, Formula)} method.
 * 
 * @author Adam
 */
@RunWith(Parameterized.class)
public class FormulaStructureCheckerTest {

    private @NonNull Formula f1;
    
    private @NonNull Formula f2;
    
    private boolean equal;

    /**
     * Creates this test.
     * 
     * @param f1 The first formula to compare.
     * @param f2 The second formula to compare.
     * @param equal Whether the two formulas should be found to be equal.
     */
    public FormulaStructureCheckerTest(@NonNull Formula f1, @NonNull Formula f2, boolean equal) {
        this.f1 = f1;
        this.f2 = f2;
        this.equal = equal;
    }
    
    
    /**
     * Checks the <code>equal(f1, f2) == equal</code> and <code>equal(f2, f1) == equal</code>.
     */
    @Test
    public void checkEqual() {
        assertThat(FormulaStructureChecker.isStructurallyEqual(f1, f2), is(equal));
        assertThat(FormulaStructureChecker.isStructurallyEqual(f2, f1), is(equal));
    }
    
    /**
     * Creates the parameters for this test.
     * 
     * @return The parameters of this test.
     */
    // CHECKSTYLE:OFF
    @Parameters(name = "equal({0}, {1}) == {2}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
            // simple constants
            {True.INSTANCE, True.INSTANCE, true},
            {False.INSTANCE, False.INSTANCE, true},
            {True.INSTANCE, False.INSTANCE, false},
            
            // simple variables
            {new Variable("A"), new Variable("A"), true},
            {new Variable("A"), new Variable("A=1"), false},
            {new Variable("A"), True.INSTANCE, false},
            {new Variable("A"), False.INSTANCE, false},
            
            // simple negations
            {not("A"), not("A"), true},
            {not("A"), new Variable("A"), false},
            {not("A"), not(True.INSTANCE), false},
            {not("A"), not("B"), false},
            
            
            // simple disjunctions
            {or("A", "B"), or("A", "B"), true},
            {or("A", or("B", "C")), or(or("A", "B"), "C"), true},
            {or("A", or("B", "C")), or(or("A", "C"), "B"), true},
            {or("A", "B"), or("A", "D"), false},
            {or("A", or("B", "D")), or("A", "D"), false},
            {or("A", or("B", "C")), or(or("A", "B"), "D"), false},
            
            // simple conjunctions
            {and("A", "B"), and("A", "B"), true},
            {and("A", and("B", "C")), and(and("A", "B"), "C"), true},
            {and("A", and("B", "C")), and(and("A", "C"), "B"), true},
            {and("A", "B"), and("A", "D"), false},
            {and("A", and("B", "D")), and("A", "D"), false},
            {and("A", and("B", "C")), and(and("A", "B"), "D"), false},
            
            // complex
            // A && (!(C && D) || B || D) == (D || !(D && C) || B) && A
            {and("A", or(not(and("C", "D")), or("B", "D"))), and(or("D", or(not(and("D", "C")), "B")), "A"), true}
            
        });
    }
    // CHECKSTYLE:ON
    
}
