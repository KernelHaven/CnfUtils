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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityModelDescriptor;
import net.ssehub.kernel_haven.variability_model.VariabilityModelDescriptor.ConstraintFileType;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

/**
 * This class tests if the VM is correctly convertet to Cnf.
 * 
 * @author Johannes
 * @author Malek
 * @author Alice
 * @author Adam
 */
public class VmToCnfConverterTest {

    /**
     * This Method tests if the given variables are completely used in the Converter.
     * If it is not used the iteration will detect it in the end.
     * It tests if the number of rows is equal to the file and if the elements are complete.
     * 
     * @throws FormatException unwanted.
     */
    @Test
    public void testCompleteVariableUse() throws FormatException {
        // Preconditions
        Set<VariabilityVariable> set = new HashSet<VariabilityVariable>();
        @SuppressWarnings("null")
        @NonNull String[] variables = {"ALPHA", "ALPHA_MODULE", "GAMMA", "BETA_MODULE", "BETA"};
        for (int i = 0; i < variables.length; i++) {
            set.add(new VariabilityVariable(variables[i], "bool", i + 1));
        }
        
        // Converting
        VariabilityModel vm = new VariabilityModel(new File("testdata/vm_to_cnf_converter/testmodel.dimacs"), set);
        vm.getDescriptor().setConstraintFileType(ConstraintFileType.DIMACS);
        Cnf cnf = new VmToCnfConverter().convertVmToCnf(vm);
        
        // Detect if used
        Set<String> varNames = cnf.getAllVarNames();
        for (int i = 0; i < variables.length; i++) {
            assertThat(variables[i] + " in index " + i, varNames.contains(variables[i]), is(true));
        }
        
        // Detect if number of rows is equal to file
        assertEquals(cnf.getRowCount(), 7);
        assertEquals(cnf.getRow(0).size(), 2);
        assertEquals(cnf.getRow(1).size(), 2);
        assertEquals(cnf.getRow(2).size(), 5);
        assertEquals(cnf.getRow(3).size(), 2);
        assertEquals(cnf.getRow(4).size(), 5);
        assertEquals(cnf.getRow(5).size(), 2);
        assertEquals(cnf.getRow(6).size(), 2);
        
        // -1 2 0
        assertThat(cnf.getElement(0, 0).isNegation(), is(true));
        assertThat(cnf.getElement(0, 1).isNegation(), is(false));
        assertEquals(cnf.getElement(0, 0).getName(), variables[0]);
        assertEquals(cnf.getElement(0, 1).getName(), variables[1]);
        
        // IF SOMEONE IS BORED:
        // -3 -1 0
        // -4 -5 -4 -2 -1 0
        // -3 5 0
        // -2 -4 -5 -4 -2 0
        // -3 1 0

        // -4 5 0
        assertThat(cnf.getElement(6, 0).isNegation(), is(true));
        assertThat(cnf.getElement(6, 1).isNegation(), is(false));
        assertEquals(cnf.getElement(6, 0).getName(), variables[3]);
        assertEquals(cnf.getElement(6, 1).getName(), variables[4]);
    }

    /**
     * Tests whether a non-DIMACS file correctly throws an exception.
     * 
     * @throws FormatException wanted.
     */
    @Test(expected = FormatException.class)
    public void testNonDimacs() throws FormatException {
        VariabilityModel vm = new VariabilityModel(
                new File("testdata/vm_to_cnf_converter/wrong_format.dimacs"), new HashSet<>());
        vm.getDescriptor().setConstraintFileType(ConstraintFileType.DIMACS);
        new VmToCnfConverter().convertVmToCnf(vm);
    }
    
    /**
     * Tests whether a DIMACS file with a non-number in it correctly throws an exception.
     * 
     * @throws FormatException wanted.
     */
    @Test(expected = FormatException.class)
    public void testWrongNumber() throws FormatException {
        VariabilityModel vm = new VariabilityModel(
                new File("testdata/vm_to_cnf_converter/wrong_number.dimacs"), new HashSet<>());
        vm.getDescriptor().setConstraintFileType(ConstraintFileType.DIMACS);
        new VmToCnfConverter().convertVmToCnf(vm);
    }
    
    /**
     * Tests whether a DIMACS file with a too high number in it correctly throws an exception.
     * 
     * @throws FormatException wanted.
     */
    @Test(expected = FormatException.class)
    public void testTooHighNumber() throws FormatException {
        VariabilityModel vm = new VariabilityModel(
                new File("testdata/vm_to_cnf_converter/too_high.dimacs"), new HashSet<>());
        vm.getDescriptor().setConstraintFileType(ConstraintFileType.DIMACS);
        new VmToCnfConverter().convertVmToCnf(vm);
    }
    
    /**
     * Tests whether a DIMACS file with a wrong number of lines (too few) in it correctly throws an
     * exception.
     * 
     * @throws FormatException wanted.
     */
    @Test(expected = FormatException.class)
    public void testWrongNumberOfLinesTooFew() throws FormatException {
        VariabilityModel vm = new VariabilityModel(
                new File("testdata/vm_to_cnf_converter/wrong_lines_1.dimacs"), new HashSet<>());
        vm.getDescriptor().setConstraintFileType(ConstraintFileType.DIMACS);
        new VmToCnfConverter().convertVmToCnf(vm);
    }
    
    /**
     * Tests whether a DIMACS file with a wrong number of lines (too much) in it correctly throws an
     * exception.
     * 
     * @throws FormatException wanted.
     */
    @Test(expected = FormatException.class)
    public void testWrongNumberOfLinesTooMuch() throws FormatException {
        VariabilityModel vm = new VariabilityModel(
                new File("testdata/vm_to_cnf_converter/wrong_lines_2.dimacs"), new HashSet<>());
        vm.getDescriptor().setConstraintFileType(ConstraintFileType.DIMACS);
        new VmToCnfConverter().convertVmToCnf(vm);
    }
    
    /**
     * Tests whether variables represented by more than one DIMACS number are handled correctly.
     * 
     * @throws FormatException unwanted.
     */
    @SuppressWarnings("null")
    @Test
    public void testTristate() throws FormatException {
        Set<VariabilityVariable> set = new HashSet<>();
        set.add(new VariabilityVariable("A", "tristate", 1) {
            
            @Override
            public void getDimacsMapping(Map<Integer, String> mapping) {
                mapping.put(getDimacsNumber(), getName());
                mapping.put(2, getName() + "_MODULE");
            }
            
        });
        
        VariabilityModel vm = new VariabilityModel(
                new File("testdata/vm_to_cnf_converter/tristate.dimacs"), set);
        vm.getDescriptor().setConstraintFileType(ConstraintFileType.DIMACS);
        Cnf cnf = new VmToCnfConverter().convertVmToCnf(vm);
        
        assertThat(cnf.getRowCount(), is(1));
        
        assertThat(cnf.getRow(0).size(), is(2));
        
        assertThat(cnf.getRow(0).get(0).isNegation(), is(true));
        assertThat(cnf.getRow(0).get(0).getName(), is("A"));
        
        assertThat(cnf.getRow(0).get(1).isNegation(), is(false));
        assertThat(cnf.getRow(0).get(1).getName(), is("A_MODULE"));
    }
    
    /**
     * Tests if unnamed variables are correctly handled.
     * 
     * @throws FormatException unwanted.
     */
    @SuppressWarnings("null")
    @Test
    public void testUnnamedVariable() throws FormatException {
        VariabilityModel vm = new VariabilityModel(
                new File("testdata/vm_to_cnf_converter/tristate.dimacs"), new HashSet<>());
        vm.getDescriptor().setConstraintFileType(ConstraintFileType.DIMACS);
        
        Cnf cnf = new VmToCnfConverter().convertVmToCnf(vm);
        
        assertThat(cnf.getRowCount(), is(1));
        
        assertThat(cnf.getRow(0).size(), is(2));
        
        assertThat(cnf.getRow(0).get(0).isNegation(), is(true));
        assertThat(cnf.getRow(0).get(0).getName(), is("VARIABLE_1"));
        
        assertThat(cnf.getRow(0).get(1).isNegation(), is(false));
        assertThat(cnf.getRow(0).get(1).getName(), is("VARIABLE_2"));
    }
    
    /**
     * Tests whether a malformed "p cnf" line correctly throws an exception.
     * 
     * @throws FormatException wanted.
     */
    @Test(expected = FormatException.class)
    public void testMalformedPCNFline() throws FormatException {
        VariabilityModel vm = new VariabilityModel(
                new File("testdata/vm_to_cnf_converter/wrong_p_cnf.dimacs"), new HashSet<>());
        vm.getDescriptor().setConstraintFileType(ConstraintFileType.DIMACS);
        new VmToCnfConverter().convertVmToCnf(vm);
    }
    
    /**
     * Tests whether a non existing DIMACS file correctly throws an exception.
     * 
     * @throws FormatException wanted.
     */
    @Test(expected = FormatException.class)
    public void testNonExistingFile() throws FormatException {
        VariabilityModel vm = new VariabilityModel(
                new File("testdata/vm_to_cnf_converter/doesnt_exist.dimacs"), new HashSet<>());
        vm.getDescriptor().setConstraintFileType(ConstraintFileType.DIMACS);
        new VmToCnfConverter().convertVmToCnf(vm);
    }
    
    /**
     * Tests whether a wrong {@link VariabilityModelDescriptor} correctly throws an exception.
     * 
     * @throws FormatException wanted.
     */
    @Test(expected = FormatException.class)
    public void testWrongDescripor() throws FormatException {
        VariabilityModel vm = new VariabilityModel(
                new File("testdata/vm_to_cnf_converter/testmodel.dimacs"), new HashSet<>());
        new VmToCnfConverter().convertVmToCnf(vm);
    }
    
}
