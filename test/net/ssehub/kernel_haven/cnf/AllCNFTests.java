package net.ssehub.kernel_haven.cnf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for CNF package.
 */
@RunWith(Suite.class)
@SuiteClasses({
    VmToCnfConverterTest.class,
    SatSolverTest.class,
    CnfTest.class,
    CnfConverterTest.class,
    CachedSatSolverTest.class,
    })
public class AllCNFTests {

}