package net.ssehub.kernel_haven;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kernel_haven.cnf.AllCNFTests;
import net.ssehub.kernel_haven.logic_utils.AllLogicTests;

/**
 * Test suite for all tests (entry point).
 */
@RunWith(Suite.class)
@SuiteClasses({AllCNFTests.class, AllLogicTests.class})
public class AllTests {

}
