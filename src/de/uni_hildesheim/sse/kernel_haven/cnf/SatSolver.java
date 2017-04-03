package de.uni_hildesheim.sse.kernel_haven.cnf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * SAT solver.
 * 
 * @author Johannes
 * @author Malek
 * @author Kevin
 */
public class SatSolver {
    
    private IVec<IVecInt> clauses;
    
    private Map<String, Integer> mapping;
    
    private Integer maxMapping;
    
    /**
     * Creates a new and empty Sat solver.
     */
    public SatSolver() {
    }
    
    
    /**
     * Creates a SAT solver with the given CNF. This CNF will be used as a basis for each successive call
     * to isSatisfiable(). This version is more performant if the same CNF is checked against a lot
     * of other CNFs.
     * 
     * @param cnf The base CNF.
     */
    public SatSolver(Cnf cnf) {
        mapping = getMapping(cnf);
        
        maxMapping = 0;
        for (Integer entry : mapping.values()) {
            if (entry > maxMapping) {
                maxMapping = entry;
            }
        }
        
        clauses = getClauses(cnf, mapping);
    }
    
    /**
     * Uses a Cnf file and check if it is satisfiable.
     * 
     * @param cnf
     *            the CNF in intern format @see Cnf.
     * @return true if satisfiable.
     * @throws SolverException If the sat solver fails.
     */
    public boolean isSatisfiable(Cnf cnf) throws SolverException {
        Map<String, Integer> numberVarMapping = getMapping(cnf);

        boolean sat = false;
        
        try {
            ISolver solver = createSolver();


            IVec<IVecInt> clauses = getClauses(cnf, numberVarMapping);
            
            solver.addAllClauses(clauses);
            
            try {
                sat = solver.isSatisfiable();
            } catch (TimeoutException e) {
                throw new SolverException(e);
            }
        } catch (ContradictionException e) {
            // sat is already false
        }
        
        return sat;
    }
    
    /**
     * Creates a new solver. If a pre-existing CNF was defined, then it is added to the solver.
     * 
     * @return The solver that can be used.
     * @throws ContradictionException If the pre-existing CNF already has a contradiction.
     */
    private ISolver createSolver() throws ContradictionException {
        ISolver solver = SolverFactory.newDefault();
        solver.setDBSimplificationAllowed(false);
        
        if (clauses != null) {
            solver.addAllClauses(clauses);
        }
        
        return solver;
    }
    
    /**
     * Creates a number mapping for the given CNF. This also includes the pre-existing CNF if it 
     * was defined.
     * 
     * @param cnf The CNF to create the mapping for.
     * @return The mapping for the given CNF (plus the pre-existing).
     */
    private Map<String, Integer> getMapping(Cnf cnf) {
        Map<String, Integer> numberVarMapping = new HashMap<>();
        int i = 1;
        
        if (this.mapping != null) {
            numberVarMapping.putAll(this.mapping);
            i = maxMapping + 1;
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
    private IVec<IVecInt> getClauses(Cnf cnf, Map<String, Integer> numberMapping) {

        VecInt[] result = new VecInt[cnf.getRowCount()];
        
        for (int i = 0; i < cnf.getRowCount(); i++) {
            List<CnfVariable> variables = cnf.getRow(i);
            int[] row = new int[variables.size()];
            
            for (int j = 0; j < variables.size(); j++) {
                Integer number = numberMapping.get(variables.get(j).getName());
                
                row[j] = !variables.get(j).isNegation() ? number : -number;

            }
            
            result[i] = new VecInt(row);
        }
        
        return new Vec<>(result);
    }
    
}
