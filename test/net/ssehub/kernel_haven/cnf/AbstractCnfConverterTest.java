package net.ssehub.kernel_haven.cnf;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Test suite to test instances of the {@link IFormulaToCnfConverter}.
 * @author Adam
 *
 */
public abstract class AbstractCnfConverterTest {
    
    /**
     * Creates the instance of the {@link IFormulaToCnfConverter} to test by the sub test suite.
     * @return The instance of the {@link IFormulaToCnfConverter} to test
     */
    protected abstract @NonNull IFormulaToCnfConverter createConverter();

    /**
     * Tests whether a formula is translated correctly.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testConverter1() throws SolverException, ConverterException {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        
        // a | b | (a || !b) && !a
        //---+---+-----------------
        // 0 | 0 | 1
        // 0 | 1 | 0
        // 1 | 0 | 0
        // 1 | 1 | 0
        Formula bool = new Conjunction(new Disjunction(a, new Negation(b)), new Negation(a));
        
        IFormulaToCnfConverter converter = createConverter();
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
     * Tests whether a formula is translated correctly.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testConverter2() throws SolverException, ConverterException {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        
        // a | b | (a && !b) || (!a && b)
        //---+---+-----------------
        // 0 | 0 | 0
        // 0 | 1 | 1
        // 1 | 0 | 1
        // 1 | 1 | 0
        Formula bool = new Disjunction(new Conjunction(a, new Negation(b)), new Conjunction(new Negation(a), b));
        
        IFormulaToCnfConverter converter = createConverter();
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
     * Tests whether constants are converted correctly.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testConverterConstants() throws SolverException, ConverterException {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        
        // a | b | ((a && !b) || false) || !true
        //---+---+-----------------
        // 0 | 0 | 0
        // 0 | 1 | 0
        // 1 | 0 | 1
        // 1 | 1 | 0
        Formula bool = new Disjunction(new Disjunction(new Conjunction(a, new Negation(b)), False.INSTANCE),
                new Negation(True.INSTANCE));
        
        IFormulaToCnfConverter converter = createConverter();
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
     * Tests whether double negations are converted correctly.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testConverterDoubleNegation() throws SolverException, ConverterException {
        Variable a = new Variable("A");
        
        // a | !!a
        //---+-----
        // 0 | 0
        // 0 | 1
        Formula bool = new Negation(new Negation(a));
        
        IFormulaToCnfConverter converter = createConverter();
        Cnf cnf = converter.convert(bool);
        
        Cnf aCnf = new Cnf();
        aCnf.addRow(new CnfVariable(false, "A"));
        
        Cnf notACnf = new Cnf();
        notACnf.addRow(new CnfVariable(true, "A"));
        
        SatSolver solver = new SatSolver();
        
        assertThat(solver.isSatisfiable(cnf.combine(aCnf)), is(true));
        assertThat(solver.isSatisfiable(cnf.combine(notACnf)), is(false));
    }
    
    /**
     * Tests whether a negated or is translated correctly.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testNegatedOr() throws SolverException, ConverterException {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        
        // a | b | !(a || b)
        //---+---+-----------------
        // 0 | 0 | 1
        // 0 | 1 | 0
        // 1 | 0 | 0
        // 1 | 1 | 0
        Formula bool = new Negation(new Disjunction(a, b));
        
        IFormulaToCnfConverter converter = createConverter();
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
     * Tests whether a negated or is translated correctly.
     * 
     * @throws SolverException unwanted.
     * @throws ConverterException unwanted.
     */
    @Test
    public void testNegatedAnd() throws SolverException, ConverterException {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        
        // a | b | !(a || b)
        //---+---+-----------------
        // 0 | 0 | 1
        // 0 | 1 | 1
        // 1 | 0 | 1
        // 1 | 1 | 0
        Formula bool = new Negation(new Conjunction(a, b));
        
        IFormulaToCnfConverter converter = createConverter();
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
        assertThat(solver.isSatisfiable(cnf.combine(notAB)), is(true));
        assertThat(solver.isSatisfiable(cnf.combine(anotB)), is(true));
        assertThat(solver.isSatisfiable(cnf.combine(aB)), is(false));
    }
    
}
