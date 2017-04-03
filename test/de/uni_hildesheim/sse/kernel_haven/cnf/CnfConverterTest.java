package de.uni_hildesheim.sse.kernel_haven.cnf;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.uni_hildesheim.sse.kernel_haven.util.logic.Conjunction;
import de.uni_hildesheim.sse.kernel_haven.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_haven.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_haven.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_haven.util.logic.True;
import de.uni_hildesheim.sse.kernel_haven.util.logic.Variable;

/**
 * Test class for the CnfConverters.
 * 
 * @author Adam
 * @author Johannes
 */
public class CnfConverterTest {
    
    /**
     * Tests whether the given CNF converter converts correctly to CNF.
     * 
     * @param converter The converter to test.
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    public void testConverter1(IFormulaToCnfConverter converter) throws SolverException, ConverterException {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        
        // a | b | (a || !b) && !a
        //---+---+-----------------
        // 0 | 0 | 1
        // 0 | 1 | 0
        // 1 | 0 | 0
        // 1 | 1 | 0
        Formula bool = new Conjunction(new Disjunction(a, new Negation(b)), new Negation(a));
        
        Cnf cnf = converter.convert(bool);
        
        Cnf notAnotB = new Cnf();
        notAnotB.addRow(new CnfVariable(true, "A"));
        notAnotB.addRow(new CnfVariable(true, "B"));
        
        Cnf notAB = new Cnf();
        notAB.addRow(new CnfVariable(true, "A"));
        notAB.addRow(new CnfVariable(false, "B"));
        
        Cnf anotB = new Cnf();
        anotB.addRow(new CnfVariable(false, "A"));
        anotB.addRow(new CnfVariable(true, "B"));
        
        Cnf aB = new Cnf();
        aB.addRow(new CnfVariable(false, "A"));
        aB.addRow(new CnfVariable(false, "B"));
        
        SatSolver solver = new SatSolver();
        
        assertThat(solver.isSatisfiable(cnf.combine(notAnotB)), is(true));
        assertThat(solver.isSatisfiable(cnf.combine(notAB)), is(false));
        assertThat(solver.isSatisfiable(cnf.combine(anotB)), is(false));
        assertThat(solver.isSatisfiable(cnf.combine(aB)), is(false));
    }
    
    /**
     * Tests whether the given CNF converter converts correctly to CNF.
     * 
     * @param converter The converter to test.
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    public void testConverter2(IFormulaToCnfConverter converter) throws SolverException, ConverterException {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        
        // a | b | (a && !b) || (!a && b)
        //---+---+-----------------
        // 0 | 0 | 0
        // 0 | 1 | 1
        // 1 | 0 | 1
        // 1 | 1 | 0
        Formula bool = new Disjunction(new Conjunction(a, new Negation(b)), new Conjunction(new Negation(a), b));
        
        Cnf cnf = converter.convert(bool);
        
        Cnf notAnotB = new Cnf();
        notAnotB.addRow(new CnfVariable(true, "A"));
        notAnotB.addRow(new CnfVariable(true, "B"));
        
        Cnf notAB = new Cnf();
        notAB.addRow(new CnfVariable(true, "A"));
        notAB.addRow(new CnfVariable(false, "B"));
        
        Cnf anotB = new Cnf();
        anotB.addRow(new CnfVariable(false, "A"));
        anotB.addRow(new CnfVariable(true, "B"));
        
        Cnf aB = new Cnf();
        aB.addRow(new CnfVariable(false, "A"));
        aB.addRow(new CnfVariable(false, "B"));
        
        SatSolver solver = new SatSolver();
        
        assertThat(solver.isSatisfiable(cnf.combine(notAnotB)), is(false));
        assertThat(solver.isSatisfiable(cnf.combine(notAB)), is(true));
        assertThat(solver.isSatisfiable(cnf.combine(anotB)), is(true));
        assertThat(solver.isSatisfiable(cnf.combine(aB)), is(false));
    }
    
    /**
     * Tests whether the given CNF converter converts correctly to CNF containing constants.
     * 
     * @param converter The converter to test.
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    public void testConverterConstants(IFormulaToCnfConverter converter) throws SolverException, ConverterException {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        
        // a | b | (a && !b) || !true
        //---+---+-----------------
        // 0 | 0 | 0
        // 0 | 1 | 0
        // 1 | 0 | 1
        // 1 | 1 | 0
        Formula bool = new Disjunction(new Conjunction(a, new Negation(b)), new Negation(new True()));
        
        Cnf cnf = converter.convert(bool);
        
        Cnf notAnotB = new Cnf();
        notAnotB.addRow(new CnfVariable(true, "A"));
        notAnotB.addRow(new CnfVariable(true, "B"));
        
        Cnf notAB = new Cnf();
        notAB.addRow(new CnfVariable(true, "A"));
        notAB.addRow(new CnfVariable(false, "B"));
        
        Cnf anotB = new Cnf();
        anotB.addRow(new CnfVariable(false, "A"));
        anotB.addRow(new CnfVariable(true, "B"));
        
        Cnf aB = new Cnf();
        aB.addRow(new CnfVariable(false, "A"));
        aB.addRow(new CnfVariable(false, "B"));
        
        SatSolver solver = new SatSolver();
        
        assertThat(solver.isSatisfiable(cnf.combine(notAnotB)), is(false));
        assertThat(solver.isSatisfiable(cnf.combine(notAB)), is(false));
        assertThat(solver.isSatisfiable(cnf.combine(anotB)), is(true));
        assertThat(solver.isSatisfiable(cnf.combine(aB)), is(false));
    }
    
    /**
     * Tests the RecursiveCnfConverter with the testConverter() method.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testRecursiveCnfConverter1() throws SolverException, ConverterException {
        testConverter1(new RecursiveCnfConverter());
    }

    /**
     * Tests the RecursiveCnfConverter with the testConverter() method.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testRecursiveCnfConverter2() throws SolverException, ConverterException {
        testConverter2(new RecursiveCnfConverter());
    }
    
    /**
     * Tests the RecursiveCnfConverter with the testConverter() method.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testRecursiveCnfConverterConstants() throws SolverException, ConverterException {
        testConverterConstants(new RecursiveCnfConverter());
    }
    
    
    /**
     * Tests the RecursiveReplacingCnfConverter with the testConverter() method.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testRecursiveReplacingCnfConverter1() throws SolverException, ConverterException {
        testConverter1(new RecursiveReplacingCnfConverter());
    }

    /**
     * Tests the RecursiveReplacingCnfConverter with the testConverter() method.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testRecursiveReplacingCnfConverter2() throws SolverException, ConverterException {
        testConverter2(new RecursiveReplacingCnfConverter());
    }
    
    /**
     * Tests the RecursiveReplacingCnfConverter with the testConverter() method.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testRecursiveReplacingCnfConverterConstants() throws SolverException, ConverterException {
        testConverterConstants(new RecursiveReplacingCnfConverter());
    }
}
