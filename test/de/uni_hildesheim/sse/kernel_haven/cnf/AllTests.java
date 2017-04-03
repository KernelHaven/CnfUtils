package de.uni_hildesheim.sse.kernel_haven.cnf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All test class.
 */
@RunWith(Suite.class)
@SuiteClasses({
    VmToCnfConverterTest.class,
    SatSolverTest.class,
    CnfTest.class,
    CnfConverterTest.class,
    })
public class AllTests {

}
