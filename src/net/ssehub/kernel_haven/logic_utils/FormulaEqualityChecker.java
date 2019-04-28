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
package net.ssehub.kernel_haven.logic_utils;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;

import net.ssehub.kernel_haven.cnf.ConverterException;
import net.ssehub.kernel_haven.cnf.FormulaToCnfConverterFactory;
import net.ssehub.kernel_haven.cnf.FormulaToCnfConverterFactory.Strategy;
import net.ssehub.kernel_haven.cnf.IFormulaToCnfConverter;
import net.ssehub.kernel_haven.cnf.ISatSolver;
import net.ssehub.kernel_haven.cnf.SatSolverFactory;
import net.ssehub.kernel_haven.cnf.SolverException;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A class for checking the logical equality of two formulas.
 * 
 * @author Adam
 */
public class FormulaEqualityChecker {

    private ISatSolver solver;
    
    private IFormulaToCnfConverter cnfConverter;

    /**
     * Creates an instance.
     */
    public FormulaEqualityChecker() {
        this.solver = SatSolverFactory.createSolver();
        this.cnfConverter = FormulaToCnfConverterFactory.create(Strategy.RECURISVE_REPLACING);
    }
    
    
    /**
     * Checks if the two given {@link Formula}s are logically equal. They are equal iff
     * {@code sat((f1 && !f2) || (!f1 && f2)) = false}.
     * 
     * @param f1 The first formula.
     * @param f2 The second formula.
     * 
     * @return Whether the two {@link Formula}s are logically equal.
     * 
     * @throws ConverterException If converting the formula(s) to CNF fails.
     * @throws SolverException If executing the sat solver fails.
     */
    public boolean isLogicallyEqual(@NonNull Formula f1, @NonNull Formula f2)
            throws SolverException, ConverterException {
        
        Formula toCheck = or(and(f1, not(f2)), and(not(f1), f2));
        
        return !solver.isSatisfiable(cnfConverter.convert(toCheck));
    }
    
}
