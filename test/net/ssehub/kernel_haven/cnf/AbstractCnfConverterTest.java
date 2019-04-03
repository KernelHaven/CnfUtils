/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ssehub.kernel_haven.cnf;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
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
        // a | b | (a || !b) && !a
        //---+---+-----------------
        // 0 | 0 | 1
        // 0 | 1 | 0
        // 1 | 0 | 0
        // 1 | 1 | 0
        Formula bool = and(or("A", not("B")), not("A"));
        
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
        
        ISatSolver solver = SatSolverFactory.createSolver();
        
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
        // a | b | (a && !b) || (!a && b)
        //---+---+-----------------
        // 0 | 0 | 0
        // 0 | 1 | 1
        // 1 | 0 | 1
        // 1 | 1 | 0
        Formula bool = or(and("A", not("B")), and(not("A"), "B"));
        
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
        
        ISatSolver solver = SatSolverFactory.createSolver();
        
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
        // a | b | ((a && !b) || false) || !true
        //---+---+-----------------
        // 0 | 0 | 0
        // 0 | 1 | 0
        // 1 | 0 | 1
        // 1 | 1 | 0
        Formula bool = or(or(and("A", not("B")), False.INSTANCE), not(True.INSTANCE));
        
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
        
        ISatSolver solver = SatSolverFactory.createSolver();
        
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
        // a | !!a
        //---+-----
        // 0 | 0
        // 0 | 1
        Formula bool = not(not("A"));
        
        IFormulaToCnfConverter converter = createConverter();
        Cnf cnf = converter.convert(bool);
        
        Cnf aCnf = new Cnf();
        aCnf.addRow(new CnfVariable(false, "A"));
        
        Cnf notACnf = new Cnf();
        notACnf.addRow(new CnfVariable(true, "A"));
        
        ISatSolver solver = SatSolverFactory.createSolver();
        
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
        // a | b | !(a || b)
        //---+---+-----------------
        // 0 | 0 | 1
        // 0 | 1 | 0
        // 1 | 0 | 0
        // 1 | 1 | 0
        Formula bool = not(or("A", "B"));
        
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
        
        ISatSolver solver = SatSolverFactory.createSolver();
        
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
        // a | b | !(a || b)
        //---+---+-----------------
        // 0 | 0 | 1
        // 0 | 1 | 1
        // 1 | 0 | 1
        // 1 | 1 | 0
        Formula bool = not(and("A", "B"));
        
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
        
        ISatSolver solver = SatSolverFactory.createSolver();
        
        assertThat(solver.isSatisfiable(cnf.combine(notAnotB)), is(true));
        assertThat(solver.isSatisfiable(cnf.combine(notAB)), is(true));
        assertThat(solver.isSatisfiable(cnf.combine(anotB)), is(true));
        assertThat(solver.isSatisfiable(cnf.combine(aB)), is(false));
    }
    
}
