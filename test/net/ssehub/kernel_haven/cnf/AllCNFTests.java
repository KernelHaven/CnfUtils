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
