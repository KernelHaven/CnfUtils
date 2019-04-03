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

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Super class for all SAT solvers that execute their solver in a "single shot". That is, the underlying solver
 * does not have an internal state and is re-created for each isSatisfiable() call.
 *
 * @author Adam
 */
abstract class AbstractSingleShotSatSolver implements ISatSolver {

    private int[][] clauses;
    
    private @Nullable Map<String, Integer> mapping;
    
    private @Nullable Integer maxMapping;
    
    /**
     * Creates a new and empty Sat solver.
     */
    public AbstractSingleShotSatSolver() {
    }
    
    
    /**
     * Creates a SAT solver with the given CNF. This CNF will be used as a basis for each successive call
     * to isSatisfiable(). This version is more performant if the same CNF is checked against a lot
     * of other CNFs.
     * 
     * @param cnf The base CNF.
     */
    public AbstractSingleShotSatSolver(@NonNull Cnf cnf) {
        Map<String, Integer> mapping = getMapping(cnf);
        
        Integer maxMapping = 0;
        for (Integer entry : mapping.values()) {
            if (entry > maxMapping) {
                maxMapping = entry;
            }
        }
        
        this.clauses = getClauses(cnf, mapping);
        
        this.mapping = mapping;
        this.maxMapping = maxMapping;
    }
    
    /**
     * Checks if the given clauses are satisfiable.
     * 
     * @param numVars The number of variables used. I.e. this is the highest number in the clauses array.
     * @param clauses A list (first dimension) of clauses with variables (second dimension). Negated values are
     *      negative. The first variable is 1.
     *      
     * @return Whether this CNF is satisfiable.
     * 
     * @throws SolverException If solving this CNF fails.
     */
    protected abstract boolean isSatisfiable(int numVars, int[][] clauses) throws SolverException; 
    
    @Override
    public boolean isSatisfiable(@NonNull Cnf cnf) throws SolverException {
        Map<String, Integer> numberVarMapping = getMapping(cnf);

        int[][] newClauses = getClauses(cnf, numberVarMapping);
        
        if (this.clauses != null) {
            int[][] tmp = new int[this.clauses.length + newClauses.length][];
            System.arraycopy(this.clauses, 0, tmp, 0, this.clauses.length);
            System.arraycopy(newClauses, 0, tmp, this.clauses.length, newClauses.length);
            newClauses = tmp;
        }
        
        int maxMapping = 0;
        for (Integer entry : numberVarMapping.values()) {
            if (entry > maxMapping) {
                maxMapping = entry;
            }
        }
        
        return isSatisfiable(maxMapping, newClauses);
    }
    
    /**
     * Creates a number mapping for the given CNF. This also includes the pre-existing CNF if it 
     * was defined.
     * 
     * @param cnf The CNF to create the mapping for.
     * @return The mapping for the given CNF (plus the pre-existing).
     */
    private @NonNull Map<String, Integer> getMapping(@NonNull Cnf cnf) {
        Map<String, Integer> numberVarMapping = new HashMap<>();
        int i = 1;
        
        Map<String, Integer> mapping = this.mapping;
        if (mapping != null) {
            numberVarMapping.putAll(mapping);
            i = notNull(maxMapping) + 1;
        }
        
        for (String varName : cnf.getAllVarNames()) {
            if (!numberVarMapping.containsKey(varName)) {
                numberVarMapping.put(varName, i);
                i++;
            }
        }
        
        return numberVarMapping;
    }
    
    /**
     * Converts the given CNF into solver clauses.
     * 
     * @param cnf The CNF to convert.
     * @param numberMapping The mapping from name to integer to use.
     * @return The clauses for the solver.
     */
    private int[][] getClauses(@NonNull Cnf cnf, @NonNull Map<String, Integer> numberMapping) {

        int[][] result = new int[cnf.getRowCount()][];
        
        for (int i = 0; i < cnf.getRowCount(); i++) {
            List<CnfVariable> variables = cnf.getRow(i);
            int[] row = new int[variables.size()];
            
            for (int j = 0; j < variables.size(); j++) {
                Integer number = numberMapping.get(variables.get(j).getName());
                
                row[j] = !variables.get(j).isNegation() ? number : -number;

            }
            
            result[i] = row;
        }
        
        return result;
    }

}
