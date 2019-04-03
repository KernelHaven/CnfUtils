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

import net.ssehub.kernel_haven.cnf.ConverterException;
import net.ssehub.kernel_haven.cnf.SolverException;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link SubTreeSimplifier}.
 * 
 * @author Adam
 */
@RunWith(Parameterized.class)
public class SubTreeSimplifierTest {
    
    private @NonNull Formula inputFormula;
    
    private @NonNull Formula expected;

    /**
     * Creates a test instance.
     * 
     * @param inputFormula The input to be simplified.
     * @param expected The expected output.
     */
    public SubTreeSimplifierTest(@NonNull Formula inputFormula, @NonNull Formula expected) {
        this.inputFormula = inputFormula;
        this.expected = expected;
    }
    
    /**
     * Tests that the simplification produces the expected result.
     * 
     * @throws ConverterException If equality checking fails.
     * @throws SolverException If equality checking fails.
     */
    @Test
    public void testSimplify() throws SolverException, ConverterException {
        Formula simplified = SubTreeSimplifier.simplify(inputFormula);
        assertThat(simplified, is(expected));
        assertThat(new FormulaEqualityChecker().isLogicallyEqual(inputFormula, simplified), is(true));
    }
    
    /**
     * Creates the parameters for this test.
     * 
     * @return The parameters of this test.
     */
    // CHECKSTYLE:OFF
    @Parameters(name = "{0} -> {1}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
            // Simple formulas that stay the same
            {True.INSTANCE, True.INSTANCE},
            {False.INSTANCE, False.INSTANCE},
            {new Variable("A"), new Variable("A")},
            {and("A", "B"), and("A", "B")},
            {or("A", "B"), or("A", "B")},
            {not("A"), not("A")},
            
            // F && !F -> False   (F = A || B)
            {and(or("A", "B"), not(or("B", "A"))), False.INSTANCE},
            // F && F -> F   (F = A || B)
            {and(or("A", "B"), or("B", "A")), or("A", "B")},
            
            // F || !F -> True   (F = A || B)
            {or(or("A", "B"), not(or("B", "A"))), True.INSTANCE},
            // F || F -> F   (F = A && B)
            {or(and("A", "B"), and("B", "A")), and("A", "B")},
            
            // complex with no simplification
            {and(or("B", not("C")), or("A", "C")), and(or("B", not("C")), or("A", "C"))},
            // complex with no simplification, but same sub-tree multiple times
            {and(or("B", not("C")), or("A", and("D", not("C")))), and(or("B", not("C")), or("A", and("D", not("C"))))}
            
        });
    }
    // CHECKSTYLE:ON

}
