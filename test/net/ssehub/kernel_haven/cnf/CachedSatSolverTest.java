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
    protected @NonNull SatSolver createSatSolver() {
        return new CachedSatSolver();
    }

    @Override
    protected @NonNull SatSolver createSatSolver(@NonNull Cnf cnf) {
        return new CachedSatSolver(cnf);
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
        
        SatSolver normalSolver = new SatSolver(huge);
        CachedSatSolver solver = new CachedSatSolver(huge);
        
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
