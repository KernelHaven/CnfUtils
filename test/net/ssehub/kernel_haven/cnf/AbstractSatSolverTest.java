package net.ssehub.kernel_haven.cnf;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * The SatSolverTest tests if the sat4j library is correct used. Therefore this
 * class tests whether a CNF is satisfiable
 * {@link SatSolverTest#testSatisfiability()} and if it is not satisfiable
 * {@link SatSolverTest#testNotSatisfiability()}. Furthermore it tests if a CNF
 * can be satisfiable and then after adding a new CNF is still satisfiable
 * {@link SatSolverTest#testCascadingSatisfiability()}. The other way around it
 * tests if a CNF is satisfiable and after adding a new CNF is not satisfiable
 * anymore. {@link SatSolverTest#testCascadingNotSatisfiability()}.
 * 
 * @author Kevin
 * @author Johannes
 * @author Alice
 */
public abstract class AbstractSatSolverTest {

    /**
     * A Cnf Variable which is not negated.
     */
    private @NonNull CnfVariable a = new CnfVariable(false, "A");

    /**
     * A Cnf Variable which is not negated.
     */
    private @NonNull CnfVariable b = new CnfVariable(false, "B");
    
    /**
     * A Cnf Variable which is not negated.
     */
    private @NonNull CnfVariable c = new CnfVariable(false, "C");

    /**
     * A Cnf Variable which is negated.
     */
    private @NonNull CnfVariable notA = new CnfVariable(true, "A");

    /**
     * A Cnf Variable which is negated.
     */
    private @NonNull CnfVariable notB = new CnfVariable(true, "B");
    
    /**
     * Creates a SAT solver for testing.
     * 
     * @return The SAT solver to test.
     */
    protected abstract @NonNull ISatSolver createSatSolver();
    
    /**
     * Creates a SAT solver for testing.
     * 
     * @param cnf The CNF to initialize the SAT solver with.
     * 
     * @return The SAT solver to test.
     */
    protected abstract @NonNull ISatSolver createSatSolver(@NonNull Cnf cnf);

    /**
     * Test satisfiability. <b>not A or B</b>
     *
     * @throws SolverException unwanted.
     */
    @Test
    public void testSatisfiability() throws SolverException {
        Cnf cnf = new Cnf();
        cnf.addRow(a, notB);
        assertThat(createSatSolver().isSatisfiable(cnf), is(true));
    }

    /**
     * Test satisfiability fail. <b>not A and A</b>
     *
     * @throws SolverException unwanted.
     */
    @Test
    public void testNotSatisfiability() throws SolverException {
        Cnf cnf = new Cnf();
        cnf.addRow(a);
        cnf.addRow(notA);
        assertThat(createSatSolver().isSatisfiable(cnf), is(false));
    }

    /**
     * Tests if <b>A</b> is satisfiable, then if <b>not A and A</b> is not satisfiable.
     *
     * @throws SolverException unwanted.
     */
    @Test
    public void testCascadingNotSatisfiability() throws SolverException {
        Cnf cnf = new Cnf();
        cnf.addRow(a);
        assertThat(createSatSolver().isSatisfiable(cnf), is(true));

        cnf.addRow(notA);
        assertThat(createSatSolver().isSatisfiable(cnf), is(false));
    }

    /**
     * Tests if <b>not A and B</b> is satisfiable and then if <b>A OR B</b> is still
     * satisfiable.
     *
     * @throws SolverException unwanted.
     */
    @Test
    public void testCascadingSatisfiability() throws SolverException {
        Cnf cnf = new Cnf();
        cnf.addRow(a, notB);
        assertThat(createSatSolver().isSatisfiable(cnf), is(true));
        cnf.addRow(notA, notB);
        assertThat(createSatSolver().isSatisfiable(cnf), is(true));
    }
    
    /**
     * Tests whether the solver works with a pre-existing CNF set.
     * 
     * @throws SolverException unwanted.
     */
    @Test
    public void testExistingCnf() throws SolverException {
        Cnf cnf1 = new Cnf();
        cnf1.addRow(a, notB);
        
        ISatSolver solver = createSatSolver(cnf1);
        Cnf cnf2 = new Cnf();
        
        // a | b | a || !b
        //---+---+---
        // 0 | 0 | 1
        // 0 | 1 | 0
        // 1 | 0 | 1
        // 1 | 1 | 1
        assertThat(solver.isSatisfiable(cnf2), is(true));
        
        cnf2.addRow(notA, notB);
        // a | b | (a || !b) && (!a || !b)
        //---+---+------------------------
        // 0 | 0 | 1
        // 0 | 1 | 0
        // 1 | 0 | 1
        // 1 | 1 | 0
        assertThat(solver.isSatisfiable(cnf2), is(true));
        
        cnf2.addRow(c);
        // a | b | (a || !b) && (!a || !b) && c
        //---+---+-----------------------------
        // 0 | 0 | 1
        // 0 | 1 | 0
        // 1 | 0 | 1
        // 1 | 1 | 0
        assertThat(solver.isSatisfiable(cnf2), is(true));
        
        cnf2.addRow(b);
        // a | b | (a || !b) && (!a || !b) && c && b
        //---+---+------------------------
        // 0 | 0 | 0
        // 0 | 1 | 0
        // 1 | 0 | 0
        // 1 | 1 | 0
        assertThat(solver.isSatisfiable(cnf2), is(false));
    }

}
