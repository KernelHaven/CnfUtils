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
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.HashMap;

import org.junit.Test;

import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;
import net.ssehub.kernel_haven.variability_model.VariabilityModelDescriptor.ConstraintFileType;

/**
 * Tests the {@link CachedSatSolver}.
 *
 * @author Adam
 */
public class CachedSatSolverTest extends AbstractSatSolverTest {

    @Override
    protected @NonNull ISatSolver createSatSolver() {
        return new CachedSatSolver(new Sat4jSolver());
    }

    @Override
    protected @NonNull ISatSolver createSatSolver(@NonNull Cnf cnf) {
        return new CachedSatSolver(new Sat4jSolver(cnf));
    }
    
    /**
     * Tests whether calls with the same CNF are actually faster.
     * 
     * @throws FormatException unwanted.
     * @throws SolverException unwanted.
     */
    @Test
    public void testCacheSpeed() throws FormatException, SolverException {
        VariabilityModel vm = new VariabilityModel(new File("testdata/huge.dimacs"),
                new HashMap<@NonNull String, VariabilityVariable>());
        vm.getDescriptor().setConstraintFileType(ConstraintFileType.DIMACS);
        Cnf huge = new VmToCnfConverter().convertVmToCnf(vm);
        
        Sat4jSolver normalSolver = new Sat4jSolver(huge);
        CachedSatSolver solver = new CachedSatSolver(new Sat4jSolver(huge));
        
        CnfVariable var1 = new CnfVariable("VARIABLE_123");
        CnfVariable notVar2 = new CnfVariable(true, "VARIABLE_231");
        Cnf test = new Cnf();
        test.addRow(var1, notVar2);
        
        // this is a really, really, bad performance test; but at least we are sure that it always succeeds ;-)
        
        for (int i = 0; i < 3; i++) {
            long tcache = System.currentTimeMillis();
            solver.isSatisfiable(test);
            tcache = System.currentTimeMillis() - tcache;
            
            long tnormal = System.currentTimeMillis();
            normalSolver.isSatisfiable(test);
            tnormal = System.currentTimeMillis() - tnormal;

            System.out.println("Round " + (i + 1));
            System.out.println("tcache: " + tcache);
            System.out.println("tnormal: " + tnormal);
            
            if (i != 0) {
                // after the first round, the cache should be much faster
                assertThat(tnormal > tcache, is(true));
            }
        }
    }

}
