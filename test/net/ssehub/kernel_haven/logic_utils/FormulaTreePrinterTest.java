package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Tests the {@link FormulaTreePrinter}.
 * 
 * @author Adam
 */
public class FormulaTreePrinterTest {

    /**
     * Tests simple {@link True}, {@link False} and {@link Variable} {@link Formula}s.
     */
    @Test
    public void testSimpleTrueFalseVar() {
        FormulaTreePrinter printer = new FormulaTreePrinter();
        
        assertThat(printer.visit(True.INSTANCE), is("true"));
        assertThat(printer.visit(False.INSTANCE), is("false"));
        assertThat(printer.visit(new Variable("ABC")), is("ABC"));
    }
    
    /**
     * Tests simple {@link Negation} around a {@link Variable}.
     */
    @Test
    public void testSimpleNegation() {
        FormulaTreePrinter printer = new FormulaTreePrinter();
        
        assertThat(printer.visit(not("ABC")), is(
                "!(\n"
                + "\tABC\n"
                + ")"));
    }
    
    /**
     * Tests simple {@link Disjunction} around a {@link Variable}.
     */
    @Test
    public void testSimpleDisjunction() {
        FormulaTreePrinter printer = new FormulaTreePrinter();
        
        assertThat(printer.visit(or("ABC", "DEF")), is(
                "\tABC\n"
                + "||\n"
                + "\tDEF"));
    }
    
    /**
     * Tests simple {@link Disjunction} around a {@link Variable}.
     */
    @Test
    public void testSimpleConjunction() {
        FormulaTreePrinter printer = new FormulaTreePrinter();
        
        assertThat(printer.visit(and("ABC", "DEF")), is(
                "\tABC\n"
                + "&&\n"
                + "\tDEF"));
    }
    
    /**
     * Tests a more complex {@link Formula}.
     */
    @Test
    public void testComplex() {
        FormulaTreePrinter printer = new FormulaTreePrinter();
        
        assertThat(printer.visit(and(or(False.INSTANCE, "ABC"), and("DEF", not(True.INSTANCE)))), is(
                "\t\tfalse\n"
                + "\t||\n"
                + "\t\tABC\n"
                + "&&\n"
                + "\t\tDEF\n"
                + "\t&&\n"
                + "\t\t!(\n"
                + "\t\t\ttrue\n"
                + "\t\t)"));
    }
    
}
