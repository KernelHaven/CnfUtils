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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;

/**
 * Tests the CNF.
 * 
 * @author Johannes
 * @author Adam
 */
public class CnfTest {

    /**
     * Tests the combine method of the cnf.
     */
    @Test
    public void testCombine() {

        Cnf cnf1 = new Cnf();
        cnf1.addRow(new CnfVariable("A"), new CnfVariable(true, "B"));
        Cnf cnf2 = new Cnf();
        cnf2.addRow(new CnfVariable(true, "C"));
        cnf2.addRow(new CnfVariable("D"));

        Cnf result = cnf1.combine(cnf2);

        assertThat(result.getRowCount(), is(3));
        assertThat(result.getRow(0).size(), is(2));
        assertThat(result.getRow(0).get(0), is(new CnfVariable("A")));
        assertThat(result.getRow(0).get(1), is(new CnfVariable(true, "B")));
        assertThat(result.getRow(1).size(), is(1));
        assertThat(result.getRow(1).get(0), is(new CnfVariable(true, "C")));
        assertThat(result.getRow(2).size(), is(1));
        assertThat(result.getRow(2).get(0), is(new CnfVariable("D")));
    }
    
    /**
     * Tests the asFormula method of the cnf.
     */
    @Test
    public void testAsFormula1() {
        Cnf cnf = new Cnf();
        cnf.addRow(new CnfVariable("A"), new CnfVariable(true, "B"));
        
        Formula formula = cnf.asFormula();
        
        assertThat(formula, is(or("A", not("B"))));
    }
    
    /**
     * Tests the asFormula method of the cnf.
     */
    @Test
    public void testAsFormula2() {
        Cnf cnf = new Cnf();
        cnf.addRow(new CnfVariable("A"));
        cnf.addRow(new CnfVariable(true, "B"), new CnfVariable("C"));
        
        Formula formula = cnf.asFormula();
        
        assertThat(formula, is(and("A", or(not("B"), "C"))));
    }
    
    /**
     * Tests the asFormula method of an empty cnf.
     */
    @Test
    public void testAsFormulaEmpty() {
        Cnf cnf = new Cnf();
        
        Formula formula = cnf.asFormula();
        
        assertThat(formula, instanceOf(True.class));
    }

}
