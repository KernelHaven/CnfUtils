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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.kernel_haven.cnf.ConverterException;
import net.ssehub.kernel_haven.cnf.SolverException;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests {@link AdamsAwesomeSimplifier}.
 * 
 * @author El-Sharkawy
 * @author Adam
 */
@RunWith(Parameterized.class)
public class AdamsAwesomeSimplifierTest {
    
    private @NonNull Formula expected;
    private @NonNull Formula inputFormula;

    /**
     * Sole constructor for the parameterized test case.
     * @param inputFormula The formula to be simplified.
     * @param expected The expected result.
     */
    public AdamsAwesomeSimplifierTest(@NonNull Formula inputFormula, @NonNull Formula expected) {
        
        this.expected = expected;
        this.inputFormula = inputFormula;
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
            {and("A", or("B", "C")), and("A", or("B", "C"))},
            {and("A", or("B", "C")), and("A", or("B", "C"))},
            
            {and("A", not(or("B", "A"))), False.INSTANCE},
            {or("A", not(and("B", "A"))), True.INSTANCE},
            
            {or(not("A"), not("B")), or(not("A"), not("B"))},
            {and(not("A"), not("B")), and(not("A"), not("B"))},
            
            {or(or("C", "A"), or(or("A", "B"), or("C", "B"))), or(or("B", "C"), "A")},
            
            // this could be simplified further to and("A", not("B"))
            {and("A", not(and("A", "B"))), and("A", not(and("A", "B")))},
        });
    }
    // CHECKSTYLE:ON
    
    /**
     * The parameterized test method.
     * 
     * @throws ConverterException If SAT-solving fails.
     * @throws SolverException If SAT-solving fails.
     */
    @Test
   public void test() throws SolverException, ConverterException {
        Formula simplified = AdamsAwesomeSimplifier.simplify(inputFormula);
        
        Assert.assertEquals(expected, simplified);
        
        assertThat(new FormulaEqualityChecker().isLogicallyEqual(simplified, inputFormula), is(true));
    }
}
