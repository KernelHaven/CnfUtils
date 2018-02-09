package net.ssehub.kernel_haven.logic_utils;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Literal;
import com.bpodgursky.jbool_expressions.Not;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;

import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.null_checks.NonNull;


/**
 * Tests the {@link FormulaToExpressionConverter}.
 * @author El-Sharkawy
 *
 */
public class FormulaToExpressionConverterTest {
    
    /**
     * Tests that an OR expression, with 1 element, can be translated into the data model of KernelHaven.
     */
    @Test
    public void testLib2KH1AryOr() {
        // Create element to test
        Expression<String> multiOr = Or.of(Variable.of("A"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormulaLib2KH(multiOr);
        Assert.assertEquals("A", translated.toString());
    }
    
    /**
     * Tests that an OR expression, with (more than) 2 elements, can be translated into the data model of KernelHaven.
     * This tests an expression with 2 elements.
     */
    @Test
    public void testLib2KH2AryOr() {
        // Create element to test
        Expression<String> multiOr = Or.of(Variable.of("A"), Variable.of("B"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormulaLib2KH(multiOr);
        Assert.assertEquals("A || B", translated.toString());
    }

    /**
     * Tests that an OR expression, with (more than) 2 elements, can be translated into the data model of KernelHaven.
     * This tests an expression with 3 elements.
     */
    @Test
    public void testLib2KH3AryOr() {
        // Create element to test
        Expression<String> multiOr = Or.of(Variable.of("A"), Variable.of("B"), Variable.of("C"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormulaLib2KH(multiOr);
        Assert.assertEquals("A || B || C", translated.toString());
    }
    
    /**
     * Tests that an OR expression, with (more than) 2 elements, can be translated into the data model of KernelHaven.
     * This tests an expression with 4 elements.
     */
    @Test
    public void testLib2KH4AryOr() {
        // Create element to test
        Expression<String> multiOr = Or.of(Variable.of("A"), Variable.of("B"), Variable.of("C"), Variable.of("D"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormulaLib2KH(multiOr);
        Assert.assertEquals("A || B || C || D", translated.toString());
    }
    
    /**
     * Tests that an AND expression, with 1 element, can be translated into the data model of KernelHaven.
     */
    @Test
    public void testLib2KH1AryAnd() {
        // Create element to test
        Expression<String> multiOr = And.of(Variable.of("A"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormulaLib2KH(multiOr);
        Assert.assertEquals("A", translated.toString());
    }
    
    /**
     * Tests that an AND expression, with (more than) 2 elements, can be translated into the data model of KernelHaven.
     * This tests an expression with 2 elements.
     */
    @Test
    public void testLib2KH2AryAnd() {
        // Create element to test
        Expression<String> multiOr = And.of(Variable.of("A"), Variable.of("B"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormulaLib2KH(multiOr);
        Assert.assertEquals("A && B", translated.toString());
    }
    
    /**
     * Tests that an AND expression, with (more than) 2 elements, can be translated into the data model of KernelHaven.
     * This tests an expression with 3 elements.
     */
    @Test
    public void testLib2KH3AryAnd() {
        // Create element to test
        Expression<String> multiOr = And.of(Variable.of("A"), Variable.of("B"), Variable.of("C"));
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormulaLib2KH(multiOr);
        Assert.assertEquals("A && B && C", translated.toString());
    }
    
    /**
     * Tests that an AND expression, with (more than) 2 elements, can be translated into the data model of KernelHaven.
     * This tests an expression with 3 elements.
     */
    @Test
    public void testLib2KH4AryAnd() {
        // Create element to test
        List<Variable<String>> vars = new LinkedList<>();
        vars.add(Variable.of("A"));
        vars.add(Variable.of("B"));
        vars.add(Variable.of("C"));
        vars.add(Variable.of("D"));
        Expression<String> multiOr = And.of(vars);
        Assert.assertNotNull(multiOr);
        
        Formula translated = translateFormulaLib2KH(multiOr);
        Assert.assertEquals("A && B && C && D", translated.toString());
    }
    
    /**
     * Tests the translation of constants true and false into the data model of KernelHaven.
     */
    @SuppressWarnings("null")
    @Test
    public void testLib2KHConstants() {
        Assert.assertSame(True.INSTANCE, translateFormulaLib2KH(Literal.getTrue()));
        Assert.assertSame(False.INSTANCE, translateFormulaLib2KH(Literal.getFalse()));
    }
    
    /**
     * Tests translation of negations into the data model of KernelHaven.
     */
    @SuppressWarnings("null")
    @Test
    public void testLib2KHNegation() {
        Assert.assertEquals(new Negation(new net.ssehub.kernel_haven.util.logic.Variable("A")), 
                translateFormulaLib2KH(Not.of(Variable.of("A"))));
    }
    
    /**
     * Translates the given JBool expression into a {@link Formula}.
     * @param expression The input expression of a test to translate.
     * @return The translated Formula
     */
    private Formula translateFormulaLib2KH(@NonNull Expression<String> expression) {
        Formula translated = null;
        try {
            translated = new FormulaToExpressionConverter().expressionToFormula(expression);
        } catch (FormatException e) {
            Assert.fail(e.getMessage());
        }
        return translated;
    }
    
    /**
     * Tests if a KernelHaven variable can be translated correctly.
     */
    @Test
    public void testKH2LibVariable() {
        Formula f = new net.ssehub.kernel_haven.util.logic.Variable("A");
        
        assertEquals("A", new FormulaToExpressionConverter().visit(f).toString());
    }
    
    /**
     * Tests if a KernelHaven conjunction can be translated correctly.
     */
    @Test
    public void testKH2LibAnd() {
        Formula f = new Conjunction(new net.ssehub.kernel_haven.util.logic.Variable("A"), 
                new net.ssehub.kernel_haven.util.logic.Variable("B"));
        
        assertEquals("(A & B)", new FormulaToExpressionConverter().visit(f).toString());
    }
    
    /**
     * Tests if a KernelHaven disjunction can be translated correctly.
     */
    @Test
    public void testKH2LibOr() {
        Formula f = new Disjunction(new net.ssehub.kernel_haven.util.logic.Variable("A"), 
                new net.ssehub.kernel_haven.util.logic.Variable("B"));
        
        assertEquals("(A | B)", new FormulaToExpressionConverter().visit(f).toString());
    }
    
    /**
     * Tests if a KernelHaven negation can be translated correctly.
     */
    @Test
    public void testKH2LibNegation() {
        Formula f = new Negation(new net.ssehub.kernel_haven.util.logic.Variable("A"));
        
        assertEquals("!A", new FormulaToExpressionConverter().visit(f).toString());
    }
    
    /**
     * Tests if a KernelHaven constants (true and false) can be translated correctly.
     */
    @Test
    public void testKH2LibConstants() {
        assertEquals("true", new FormulaToExpressionConverter().visit(True.INSTANCE).toString());
        assertEquals("false", new FormulaToExpressionConverter().visit(False.INSTANCE).toString());
    }
    
}
