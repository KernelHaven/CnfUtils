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

import static java.util.Arrays.asList;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Tests the {@link SubTreeGroupFinder}.
 * 
 * @author Adam
 */
public class SubTreeGroupFinderTest {
    
    /**
     * Tests splitting a complex formula into sub-trees.
     */
    @Test
    public void testComplex() {
        Formula f = and(and("A", not(True.INSTANCE)), not(or("B", False.INSTANCE)));
        
        SubTreeGroupFinder finder = new SubTreeGroupFinder();
        List<List<Formula>> result = finder.findGroups(f);

        assertGroups(result,
                asList(f),
                asList(or("B", False.INSTANCE)),
                asList(not(or("B", False.INSTANCE))),
                asList(not(True.INSTANCE)),
                asList(and("A", not(True.INSTANCE)))
        );
    }
    
    /**
     * Tests a {@link Formula} where a similar (and equal) sub-tree appears multiple times.
     */
    @Test
    public void testSimilarGroupAppearsMultipleTimes() {
        // A && B (== B && A) appears two times
        Formula f = or(and("A", "B"), not(and("B", "A")));
        
        SubTreeGroupFinder finder = new SubTreeGroupFinder();
        List<List<Formula>> result = finder.findGroups(f);
        
        assertGroups(result,
                asList(f),
                asList(and("A", "B"), and("B", "A")),
                asList(not(and("B", "A")))
        );
    }
    
    /**
     * Tests a {@link Formula} where the same sub-tree appears multiple times.
     */
    @Test
    public void testSameGroupAppearsMultipleTimes() {
        // A && B (== B && A) appears two times
        Formula f = or(and("A", "B"), not(and("A", "B")));
        
        SubTreeGroupFinder finder = new SubTreeGroupFinder();
        List<List<Formula>> result = finder.findGroups(f);
        
        assertGroups(result,
                asList(f),
                asList(and("A", "B"), and("A", "B")),
                asList(not(and("A", "B")))
        );
    }

    /**
     * Tests that a hash-collision of two non-equal {@link Formula} is handled correctly (i.e. they do not appear in
     * the same group).
     */
    @Test
    public void testHashCollision() {
        Variable var1 = new Variable("ab");
        Variable var2 = new Variable("\u0c21");
        
        // pre-condition: the two variables have a hash-collision
        assertThat(var1.hashCode(), is(var2.hashCode()));
        assertThat(var1.equals(var2), is(false));
        
        Disjunction f = or(and(var1, "B"), and(var2, "B"));
        
        // pre-condition: the two ands have a hash-collision
        assertThat(and(var1, "B").hashCode(), is(and(var2, "B").hashCode()));
        
        SubTreeGroupFinder finder = new SubTreeGroupFinder();
        List<List<Formula>> result = finder.findGroups(f);
        
        assertGroups(result, asList(f), asList(and(var1, "B")), asList(and(var2, "B")));
    }
    
    /**
     * Asserts that result consists of exactly the given expectedGroups.
     * 
     * @param result The result list of groups to check.
     * @param expectedGroups The list of expected groups.
     */
    @SafeVarargs
    private static void assertGroups(List<List<Formula>> result, List<Formula>... expectedGroups) {
        for (List<Formula> expectedGroup : expectedGroups) {
            boolean found = result.remove(expectedGroup);
            assertTrue("Expected group " + expectedGroup, found);
        }
        
        assertThat("Got more than the expected groups (left over: " + result + ")", result.isEmpty(), is(true));
    }
    
}
