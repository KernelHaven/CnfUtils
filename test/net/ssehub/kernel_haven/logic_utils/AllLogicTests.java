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

import java.util.Random;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Test suite for logic utils package.
 */
@RunWith(Suite.class)
@SuiteClasses({
    FormulaToExpressionConverterTest.class,
    LogicUtilsTest.class,
    SimplifyingDisjunctionQueueTest.class,
    FormulaSimplificationVisitorTest.class,
    FormulaSimplificationVisitor2Test.class,
    FormulaEqualityCheckerTest.class,
    FormulaTreePrinterTest.class,
    FormulaStructureCheckerTest.class,
    FormulaStructureCheckerTermSplitTest.class,
    SubTreeGroupFinderTest.class,
    SubTreeSimplifierTest.class,
    RandomSubTreeSimplifierTest.class,
    })
public class AllLogicTests {

    /**
     * Helper method for generating random {@link Formula}s. This generates approximately 2000 character long formulas.
     * 
     * @param random A random source.
     * @param depth The current depth. Start with 0.
     * 
     * @return A random {@link Formula}.
     */
    public static @NonNull Formula generateRandomFormula(@NonNull Random random, int depth) {
        Formula result;
        
        if (depth > 10) {
            result = new Variable("VAR_" + random.nextInt(10));
            
        } else {
            int rand = random.nextInt(100);
            
            if (rand < 30) {
                result = or(generateRandomFormula(random, depth + 1), generateRandomFormula(random, depth + 1));
                
            } else if (rand < 60) {
                result = and(generateRandomFormula(random, depth + 1), generateRandomFormula(random, depth + 1));
                
            } else if (rand < 75) {
                result = new Negation(generateRandomFormula(random, depth + 1));
                
            } else if (depth > 4 && rand < 95) {
                result = new Variable("VAR_" + random.nextInt(10));
                
            } else if (depth > 4 && rand < 98) {
                result = True.INSTANCE;
                
            } else if (depth > 4 && rand < 100) {
                result = False.INSTANCE;
                
            } else {
                result = and(generateRandomFormula(random, depth + 1), generateRandomFormula(random, depth + 1));
            }
        }
        
        return result;
    }
    
}
