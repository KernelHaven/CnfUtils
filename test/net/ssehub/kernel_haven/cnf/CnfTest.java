package net.ssehub.kernel_haven.cnf;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Tests the CNF.
 * 
 * @author Johannes
 * @author Adam
 */
public class CnfTest {

    /**
     * Tests the combine method of the cnf.
     */
    @Test
    public void testCombine() {

        Cnf cnf1 = new Cnf();
        cnf1.addRow(new CnfVariable("A"), new CnfVariable(true, "B"));
        Cnf cnf2 = new Cnf();
        cnf2.addRow(new CnfVariable(true, "C"));
        cnf2.addRow(new CnfVariable("D"));

        Cnf result = cnf1.combine(cnf2);

        assertThat(result.getRowCount(), is(3));
        assertThat(result.getRow(0).size(), is(2));
        assertThat(result.getRow(0).get(0), is(new CnfVariable("A")));
        assertThat(result.getRow(0).get(1), is(new CnfVariable(true, "B")));
        assertThat(result.getRow(1).size(), is(1));
        assertThat(result.getRow(1).get(0), is(new CnfVariable(true, "C")));
        assertThat(result.getRow(2).size(), is(1));
        assertThat(result.getRow(2).get(0), is(new CnfVariable("D")));
    }
    
    /**
     * Tests the asFormula method of the cnf.
     */
    @Test
    public void testAsFormula1() {
        Cnf cnf = new Cnf();
        cnf.addRow(new CnfVariable("A"), new CnfVariable(true, "B"));
        
        Formula formula = cnf.asFormula();
        
        assertThat(formula, is(new Disjunction(new Variable("A"), new Negation(new Variable("B")))));
    }
    
    /**
     * Tests the asFormula method of the cnf.
     */
    @Test
    public void testAsFormula2() {
        Cnf cnf = new Cnf();
        cnf.addRow(new CnfVariable("A"));
        cnf.addRow(new CnfVariable(true, "B"), new CnfVariable("C"));
        
        Formula formula = cnf.asFormula();
        
        Variable a = new Variable("A");
        Formula notB = new Negation(new Variable("B"));
        Variable c = new Variable("C");
        
        assertThat(formula, is(new Conjunction(a, new Disjunction(notB, c))));
    }
    
    /**
     * Tests the asFormula method of an empty cnf.
     */
    @Test
    public void testAsFormulaEmpty() {
        Cnf cnf = new Cnf();
        
        Formula formula = cnf.asFormula();
        
        assertThat(formula, instanceOf(True.class));
    }

}
