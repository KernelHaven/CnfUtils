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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Random;

import org.junit.Test;

import net.ssehub.kernel_haven.cnf.ConverterException;
import net.ssehub.kernel_haven.cnf.SolverException;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.parser.CStyleBooleanGrammar;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.util.logic.parser.Parser;
import net.ssehub.kernel_haven.util.logic.parser.VariableCache;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link SubTreeSimplifier} with random {@link Formula}s.
 * 
 * @author Adam
 */
@SuppressWarnings("null")
public class RandomSubTreeSimplifierTest {
    
    /**
     * Tests that simplifying random {@link Formula}s creates logically equal {@link Formula}s.
     * 
     * @throws ConverterException If equality checking fails.
     * @throws SolverException If equality checking fails.
     */
    @Test
    public void testRandom() throws SolverException, ConverterException {
        FormulaEqualityChecker checker = new FormulaEqualityChecker();
        
        for (int i = 0; i < 100; i++) {
            Formula inputFormula = AllLogicTests.generateRandomFormula(new Random(), 0);
            
            Formula simplified = SubTreeSimplifier.simplify(inputFormula);
            
            boolean equal = checker.isLogicallyEqual(inputFormula, simplified);
            
            if (!equal) {
                System.out.println(inputFormula + " != " + simplified);
            }
            assertThat(inputFormula + " != " + simplified, equal, is(true));
        }
    }
    
    /**
     * Tests {@link Formula}s that were previously problematic.
     * 
     * @throws ExpressionFormatException If parsing a formula string fails.
     * @throws ConverterException If equality checking fails.
     * @throws SolverException If equality checking fails.
     */
    @Test
    public void testPreviouslyProblematic() throws ExpressionFormatException, SolverException, ConverterException {
        Parser<@NonNull Formula> parser = new Parser<>(new CStyleBooleanGrammar(new VariableCache()));
        
        String[] formulas = {
            // CHECKSTYLE:OFF
            "((!((((VAR_5 && !VAR_7) || (VAR_6 && (VAR_9 || VAR_5))) && (((VAR_8 || VAR_4) && 0) || !VAR_6) && (VAR_6 || ((VAR_6 || VAR_1) && VAR_3 && VAR_6) || ((VAR_5 || VAR_9 || (VAR_2 && VAR_0)) && VAR_3))) || (!(VAR_3 || 1) && (VAR_9 || VAR_5) && (VAR_8 || VAR_6) && (!VAR_1 || VAR_3)) || VAR_3) || !1 || !!(VAR_5 || (VAR_3 && (VAR_3 || VAR_5) && VAR_2))) && ((VAR_6 && VAR_9 && (VAR_0 || VAR_6 || !(VAR_6 || (VAR_3 && VAR_8))) && (VAR_4 || !(!VAR_6 && VAR_1) || (((VAR_7 && VAR_9) || VAR_7 || VAR_8) && (VAR_0 || VAR_2 || VAR_2) && ((VAR_1 && VAR_0) || (VAR_5 && VAR_4)) && (VAR_3 || VAR_6 || VAR_6 || VAR_1)))) || ((((VAR_9 || VAR_7) && (VAR_6 || VAR_8) && VAR_4) || VAR_4 || (!VAR_2 && VAR_8 && VAR_5 && (VAR_6 || VAR_3 || !VAR_6)) || ((VAR_3 || VAR_1) && VAR_7 && VAR_5) || VAR_6) && (!VAR_3 || (VAR_2 && VAR_1 && (VAR_9 || VAR_8) && !!VAR_3) || (VAR_3 && VAR_9) || !VAR_7 || (VAR_5 && VAR_8) || VAR_4 || VAR_0 || (VAR_6 && VAR_1 && VAR_7 && VAR_3))) || (!VAR_7 && (VAR_4 || (VAR_4 && VAR_7) || VAR_1) && VAR_5 && (VAR_9 || VAR_2) && !VAR_7 && (VAR_0 || VAR_4) && VAR_6)) && ((VAR_5 && (VAR_0 || (VAR_4 && VAR_7) || !VAR_9 || VAR_0 || VAR_1 || !VAR_4)) || !VAR_5 || (!VAR_1 && VAR_0) || (!(((VAR_0 && VAR_0 && (VAR_4 || VAR_9)) || (VAR_2 && VAR_7 && (VAR_6 || VAR_2))) && !((VAR_9 || VAR_5) && VAR_3) && ((!VAR_6 && (VAR_5 || VAR_2) && (VAR_6 || VAR_7) && !VAR_3) || (VAR_8 && VAR_0 && VAR_9 && VAR_5 && VAR_0 && VAR_7 && VAR_6))) && VAR_0 && VAR_0 && (((VAR_2 || VAR_5) && (VAR_6 || VAR_1) && !VAR_5 && (VAR_9 || VAR_3) && !!VAR_7) || (!VAR_3 && VAR_1) || (((VAR_6 && VAR_4) || (VAR_9 && VAR_3)) && ((VAR_5 && VAR_0) || (VAR_2 && VAR_4)))))) && ((VAR_9 && VAR_9 && !(!VAR_0 || VAR_6) && VAR_1 && VAR_4 && (VAR_3 || VAR_2) && !(VAR_6 && VAR_9) && (((VAR_0 || VAR_1) && (VAR_6 || VAR_7) && (VAR_0 || VAR_2) && (VAR_9 || VAR_7) && (VAR_8 || VAR_0 || VAR_9 || VAR_9 || VAR_8)) || VAR_5)) || !(!VAR_3 || VAR_9 || VAR_7) || (VAR_6 && VAR_8 && (VAR_2 || VAR_3)) || VAR_5 || (VAR_4 && ((VAR_1 && VAR_5) || VAR_7 || VAR_8) && VAR_4) || (VAR_7 && !(!!VAR_1 || VAR_4 || VAR_1 || VAR_8 || VAR_7)) || !(!(VAR_8 && VAR_8) || VAR_6 || VAR_5 || !!(VAR_2 && VAR_4 && (VAR_6 || VAR_1))))) || (!((VAR_4 && VAR_0) || 1) && !VAR_7 && (!(VAR_0 && VAR_3) || !VAR_4) && !VAR_0 && (!VAR_5 || !VAR_0 || VAR_4 || (VAR_9 && VAR_4 && (VAR_1 || VAR_2)) || !VAR_8 || (VAR_8 && VAR_6)) && ((VAR_4 && !(!VAR_7 && 1) && !(VAR_0 || VAR_0) && VAR_9 && VAR_1) || (!!(!VAR_8 && (VAR_1 || VAR_4)) && (VAR_3 || VAR_0 || VAR_4 || VAR_3 || (VAR_8 && VAR_1) || VAR_2 || VAR_1)))) || ((VAR_5 || VAR_2) && (VAR_7 || VAR_2 || ((VAR_1 || VAR_5) && 1 && (VAR_2 || VAR_2) && (VAR_7 || VAR_9)) || ((VAR_7 || VAR_2 || (VAR_1 && VAR_9)) && (VAR_7 || VAR_2)) || VAR_6)) || (((VAR_5 && VAR_6 && VAR_4 && VAR_0 && VAR_2 && (VAR_1 || VAR_6)) || !(VAR_9 && VAR_4) || !!VAR_2 || ((!VAR_4 || !VAR_2) && !(VAR_7 && VAR_1)) || VAR_2 || VAR_4 || (VAR_1 && VAR_4) || (VAR_2 && VAR_9) || VAR_4 || VAR_4) && !(VAR_5 || VAR_2 || VAR_0 || VAR_3 || VAR_1 || (VAR_2 && VAR_2) || !VAR_1) && VAR_6 && VAR_4 && ((VAR_8 && !!VAR_3) || VAR_5)) || (!!(VAR_0 && VAR_8 && VAR_7 && VAR_6 && !(VAR_6 && VAR_1) && (VAR_3 || VAR_8 || VAR_9) && VAR_6) && (VAR_8 || VAR_2 || !(VAR_0 && VAR_2) || VAR_5 || VAR_9 || VAR_8 || 1 || 1 || !0) && (!1 || !((VAR_7 || VAR_2 || VAR_0) && !1)) && !(!VAR_9 && (VAR_8 || (!VAR_7 && (VAR_2 || VAR_3)) || 1) && (VAR_5 || VAR_9 || (VAR_6 && VAR_6)) && 1 && !VAR_3 && !(VAR_9 || VAR_7) && 0 && VAR_8 && VAR_0 && (!VAR_6 || VAR_7)) && !(!VAR_1 && 0 && (VAR_0 || !VAR_6) && VAR_3) && 1 && (((!((VAR_1 || VAR_4) && VAR_8 && VAR_1 && ((VAR_1 && VAR_1) || VAR_6)) || VAR_0) && VAR_1) || 1))"
             // CHECKSTYLE:ON
        };
        
        
        for (String formula : formulas) {
            Formula f = parser.parse(formula);
            findBadPart(f);
        }
    }
    
    /**
     * Finds the (smallest) part of the given formula that is not simplified correctly.
     * 
     * @param formula The formula to check.
     * 
     * @throws ConverterException If equality checking fails.
     * @throws SolverException If equality checking fails.
     */
    private void findBadPart(@NonNull Formula formula) throws SolverException, ConverterException {
        if (formula instanceof Disjunction) {
            findBadPart(((Disjunction) formula).getLeft());
            findBadPart(((Disjunction) formula).getRight());
            
        } else if (formula instanceof Conjunction) {
            findBadPart(((Conjunction) formula).getLeft());
            findBadPart(((Conjunction) formula).getRight());
             
        } else if (formula instanceof Negation) {
            findBadPart(((Negation) formula).getFormula());
            
        }
        
        Formula simplified = SubTreeSimplifier.simplify(formula);
        
        FormulaEqualityChecker checker = new FormulaEqualityChecker();
        boolean equal = checker.isLogicallyEqual(formula, simplified);
        
        if (!equal) {
            System.out.println("FOUND ERROR:\n" + formula + "\n!=\n" + simplified);
        }
        assertThat(formula + " != " + simplified, equal, is(true));
    }
    
}
