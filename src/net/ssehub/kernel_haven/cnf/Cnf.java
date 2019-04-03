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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.parser.VariableCache;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * The Class Cnf Represents a CNF Table for CNF with CnfVariables as elements.
 * Performance: Initialize Arraylists to filesize, never use a linked list.
 * 
 * @author Kevin
 * @author Johannes
 * @author Malek
 * @author Alice
 * @author Adam
 */
public class Cnf {

    /**
     * The internal representation of the Cnf. The Variables in each row is
     * conjuncted. A row to each other row is disjunct.
     */
    private @NonNull List<@NonNull List<@NonNull CnfVariable>> table;

    /**
     * Initializes the Cnf with an ArrayList with an initial length. This is
     * more performant.
     * 
     * @param length
     *            the initial length.
     */
    public Cnf(int length) {
        table = new ArrayList<>(length);
    }

    /**
     * Please Use the Cnf Constructor with an initial list size. For
     * Performance.
     */
    public Cnf() {
        table = new ArrayList<>();
    }

    /**
     * ArrayList is necessary because of performance access.
     * 
     * @param row
     *            is the row with the CnfVariables to be added.
     */
    public void addRow(@NonNull CnfVariable /*@NonNull*/ ... row) {
        // TODO: commented out @NonNull annotation because checkstyle can't parse it
        this.table.add(notNull(Arrays.asList(row)));
    }

    /**
     * Gets the row. Check with {@link Cnf#getRowCount()} if index is smaller
     * than. Throws IndexOutOfBoundsException if a not existing index is called.
     * 
     * @param row
     *            the existing row index. Not null.
     * @return the row. Not null.
     */
    public @NonNull List<@NonNull CnfVariable> getRow(int row) {
        List<@NonNull CnfVariable> toReturn = null;
        toReturn = notNull(table.get(row));
        return toReturn;
    }

    /**
     * Gets the element. Throws IndexOutOfBoundException if a param is null.
     *
     * @param row
     *            is the row number of the element. Must not be null.
     * @param number
     *            is the number of the element. Must not be null.
     * @return the element which is a single cnf variable.
     */
    public @NonNull CnfVariable getElement(int row, int number) {
        CnfVariable toReturn = null;
        toReturn = notNull(notNull(table.get(row)).get(number));
        return toReturn;
    }

    /**
     * Gets the table which is a List of a List of cnf variables.
     *
     * @return the table of variables.
     */
    public @NonNull List<@NonNull List<@NonNull CnfVariable>> getTable() {
        return table;
    }

    /**
     * Gets the row count.
     * 
     * @return the row count
     */
    public int getRowCount() {
        return table.size();
    }

    /**
     * Returns all existing VariableNames to generate unique identification
     * numbers.
     * 
     * @return all variable names as a set. Not null.
     */
    public @NonNull Set<@NonNull String> getAllVarNames() {
        Set<@NonNull String> allVars = new HashSet<>();
        for (List<CnfVariable> arrayList : table) {
            for (CnfVariable cnfVariable : arrayList) {
                allVars.add(cnfVariable.getName());
            }
        }
        return allVars;
    }

    /**
     * Creates a new Cnf which contains both, this and the other given Cnf
     * combined. The two Cnf objects are combined with an implicit logical AND.
     * 
     * @param cnf
     *            the other cnf to be combined with this cnf. Must not be
     *            <code>null</code>.
     * @return a new cnf.
     */
    public @NonNull Cnf combine(@NonNull Cnf cnf) {
        Cnf result = new Cnf(cnf.getRowCount() + this.getRowCount());
        for (List<CnfVariable> list : table) {
            result.addRow(notNull(list.toArray(new @NonNull CnfVariable[0])));
        }
        for (List<CnfVariable> list : cnf.table) {
            result.addRow(notNull(list.toArray(new @NonNull CnfVariable[0])));
        }
        return result;
    }
    
    /**
     * Converts a single (possibly negated) variable to a formula.
     * 
     * @param variable The variable to convert to a formula.
     * @param cache The cache to create variables from.
     * @return The formula representing the (possibly negated) variable.
     */
    private @NonNull Formula variableAsFormula(@NonNull CnfVariable variable, @NonNull VariableCache cache) {
        Formula result = cache.getVariable(variable.getName());
        if (variable.isNegation()) {
            result = new Negation(result);
        }
        return result;
    }
    
    /**
     * Converts a single row into a formula. This simply creates a disjunction over all variables.
     * 
     * @param row The row to convert.
     * @param cache The cache to create variables from.
     * @return The formula representing the given row.
     */
    private @NonNull Formula rowAsFormula(@NonNull List<@NonNull CnfVariable> row, @NonNull VariableCache cache) {
        Formula result = variableAsFormula(row.get(0), cache);
        for (int i = 1; i < row.size(); i++) {
            result = new Disjunction(result, variableAsFormula(row.get(i), cache));
        }
        return result;
    }
    
    /**
     * Transforms this CNF into a boolean formula.
     * 
     * @return This CNF as a boolean formula.
     */
    public @NonNull Formula asFormula() {
        VariableCache cache = new VariableCache();
        
        Formula result = True.INSTANCE;
        
        if (table.size() > 0) {
            result = rowAsFormula(table.get(0), cache);
            
            for (int i = 1; i < table.size(); i++) {
                result = new Conjunction(result, rowAsFormula(table.get(i), cache));
            }
            
        }
        
        return result;
    }
    
    @Override
    public @NonNull String toString() {
        StringBuilder result = new StringBuilder();
        
        for (List<CnfVariable> row : table) {
            result.append(row).append("\n");
        }
        
        return notNull(result.toString());
    }
    
    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        int hash = 3244324;
        
        // sum up the hashes of the rows; this has the benefit that the order of the rows doesn't matter for hash
        // (since it also doesn't matter for semantics)
        for (List<CnfVariable> row : table) {
            
            int lineHash = 675433;
            
            // again, simply sum up the CnfVariables, since order doesn't matter semantically
            for (CnfVariable variable : row) {
                lineHash += variable.hashCode(); 
            }
            
            hash += lineHash * 5434543;
        }
        
        return hash * 573495334;
    }
    
}
