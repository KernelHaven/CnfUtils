package net.ssehub.kernel_haven.logic_utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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
    })
public class AllLogicTests {

}
