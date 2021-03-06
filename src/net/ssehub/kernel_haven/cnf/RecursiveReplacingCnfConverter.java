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

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Introduces new variables to simplify overall constraint.
 * Resulting constraints are equisatisfiable.
 * 
 * Reduces the number of resulting constraints from (worst case) exponential growth to
 * quadratic growth.
 * 
 * @author Adam (copied from KernelMiner project)
 * @author Johannes
 */
public class RecursiveReplacingCnfConverter extends RecursiveCnfConverter {
    
    private int uniqueCounter = 1;
    
    @Override
    protected @NonNull Cnf handleOr(@NonNull Disjunction call) throws ConverterException {
        /*
         * We have call = P v Q
         * 
         * If P and Q both are "complex", then we do the following to simplify the constraint:
         * 
         *     We introduce a new variable, Z, such that (~Z v P) ^ (Z v Q).
         *     This is satisfiable if, and only if, call is satisfiable.
         *     
         * A formula is complex, if it contains more than one variable.
         */
        
        Cnf result = null;
        
        if (isComplex(call.getLeft()) && isComplex(call.getRight())) {
            Variable z = new Variable("temp_" + (uniqueCounter++));
            
            Formula left = new Disjunction(not(z), call.getLeft());
            Formula right = new Disjunction(z, call.getRight());
            
            result = convertPrivate(new Conjunction(left, right));
        } else {
            result = super.handleOr(call);
        }
        
        
        return result;
    }
    
    /**
     * A formula is complex, if it contains more than one variable.
     * 
     * @param tree The formula to analyse.
     * @return <tt>true</tt> if tree contains more than one variable
     * @throws ConverterException If the formula contains unexpected elements.
     */
    private boolean isComplex(@NonNull Formula tree) throws ConverterException {
        boolean result;
        
        if (tree instanceof Variable) {
            result = false;
            
        } else if (tree instanceof Negation) {
            result = isComplex(((Negation) tree).getFormula());
            
        } else if (tree instanceof Conjunction || tree instanceof Disjunction) {
            result = true;
            
        } else {
            throw new ConverterException("Invalid element in tree: " + tree);
        }
        
        return result;
    }

}
