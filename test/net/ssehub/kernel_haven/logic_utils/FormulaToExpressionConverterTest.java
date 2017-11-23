package net.ssehub.kernel_haven.logic_utils;

import org.junit.Assert;
import org.junit.Test;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;

import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.logic.Formula;


/**
 * Tests the {@link FormulaToExpressionConverter}.
 * @author El-Sharkawy
 *
 */
public class FormulaToExpressionConverterTest {
    
    /**
     * Tests that an OR expression, with (more than) 2 elements, can be translated into the data model of KernelHaven.
     * This tests an expression with 2 elements.
     */
    @Test
    public void test2AryOr() {
        // Create element to test
        Expression<String> multiOr = Or.of(Variable.of("A"), Variable.of("B"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormula(multiOr);
        Assert.assertEquals("A || B", translated.toString());
    }

    /**
     * Tests that an OR expression, with (more than) 2 elements, can be translated into the data model of KernelHaven.
     * This tests an expression with 3 elements.
     */
    @Test
    public void test3AryOr() {
        // Create element to test
        Expression<String> multiOr = Or.of(Variable.of("A"), Variable.of("B"), Variable.of("C"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormula(multiOr);
        Assert.assertEquals("A || B || C", translated.toString());
    }
    
    /**
     * Tests that an OR expression, with (more than) 2 elements, can be translated into the data model of KernelHaven.
     * This tests an expression with 4 elements.
     */
    @Test
    public void test4AryOr() {
        // Create element to test
        Expression<String> multiOr = Or.of(Variable.of("A"), Variable.of("B"), Variable.of("C"), Variable.of("D"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormula(multiOr);
        Assert.assertEquals("A || B || C || D", translated.toString());
    }
    
    /**
     * Tests that an AND expression, with (more than) 2 elements, can be translated into the data model of KernelHaven.
     * This tests an expression with 2 elements.
     */
    @Test
    public void test2AryAnd() {
        // Create element to test
        Expression<String> multiOr = And.of(Variable.of("A"), Variable.of("B"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormula(multiOr);
        Assert.assertEquals("A && B", translated.toString());
    }
    
    /**
     * Tests that an AND expression, with (more than) 2 elements, can be translated into the data model of KernelHaven.
     * This tests an expression with 3 elements.
     */
    @Test
    public void test3AryAnd() {
        // Create element to test
        Expression<String> multiOr = And.of(Variable.of("A"), Variable.of("B"), Variable.of("C"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormula(multiOr);
        Assert.assertEquals("A && B && C", translated.toString());
    }
    
    /**
     * Translates the given JBool expression into a {@link Formula}.
     * @param expression The input expression of a test to translate.
     * @return The translated Formula
     */
    private Formula translateFormula(Expression<String> expression) {
        Formula translated = null;
        try {
            translated = new FormulaToExpressionConverter().expressionToFormula(expression);
        } catch (FormatException e) {
            Assert.fail(e.getMessage());
        }
        return translated;
    }
}
