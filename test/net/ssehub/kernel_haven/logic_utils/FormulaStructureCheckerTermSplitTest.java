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
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link FormulaStructureChecker#getAllConjunctionTerms(Conjunction)} and
 * {@link FormulaStructureChecker#getAllDisjunctionTerms(Disjunction)} methods.
 * 
 * @author Adam
 */
@RunWith(Parameterized.class)
public class FormulaStructureCheckerTermSplitTest {

    private @NonNull Formula formula;
    
    private @NonNull Formula @NonNull [] terms;
    
    /**
     * Creates this test.
     * 
     * @param formula The formula to split.
     * @param terms The expected terms
     */
    public FormulaStructureCheckerTermSplitTest(@NonNull Formula formula, @NonNull Formula @NonNull [] terms) {
        this.formula = formula;
        this.terms = terms;
    }
    
    
    /**
     * Checks the <code>equal(f1, f2) == equal</code> and <code>equal(f2, f1) == equal</code>.
     */
    @Test
    public void checkEqual() {
        List<@NonNull Formula> split;
        if (formula instanceof Conjunction) {
            split = FormulaStructureChecker.getAllConjunctionTerms((Conjunction) formula);
            
        } else { // disjunction
            split = FormulaStructureChecker.getAllDisjunctionTerms((Disjunction) formula);
        }
        
        for (Formula f : terms) {
            boolean found = split.remove(f);
            assertTrue("Expected " + f + " to be a term", found);
        }
        
        assertThat(split.isEmpty(), is(true));
    }
    
    /**
     * Creates the parameters for this test.
     * 
     * @return The parameters of this test.
     */
    // CHECKSTYLE:OFF
    @Parameters(name = "{0}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
            {or("A", "B"), new Formula[] {new Variable("A"), new Variable("B")}},
            {or("A", or("B", "C")), new Formula[] {new Variable("A"), new Variable("B"), new Variable("C")}},
            {or("A", and("B", "C")), new Formula[] {new Variable("A"), and("B", "C")}},
            {or(or("A", "D"), or(or("B", "E"), "C")), new Formula[] {new Variable("A"), new Variable("B"),
                new Variable("C"), new Variable("D"), new Variable("E")}},
            
            {and("A", "B"), new Formula[] {new Variable("A"), new Variable("B")}},
            {and("A", and("B", "C")), new Formula[] {new Variable("A"), new Variable("B"), new Variable("C")}},
            {and("A", or("B", "C")), new Formula[] {new Variable("A"), or("B", "C")}},
            {and(and("A", "D"), and(and("B", "E"), "C")), new Formula[] {new Variable("A"), new Variable("B"),
                    new Variable("C"), new Variable("D"), new Variable("E")}}
        });
    }
    // CHECKSTYLE:ON
    
}
