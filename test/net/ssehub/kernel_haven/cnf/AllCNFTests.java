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
    Sat4jSolverTest.class,
    CryptoMiniSatSolverTest.class,
    CnfTest.class,
    RecursiveCnfConverterTest.class,
    RecursiveReplacingCnfConverterTest.class,
    CachedSatSolverTest.class,
    FormulaToCnfConverterFactoryTest.class,
    })
public class AllCNFTests {

}
