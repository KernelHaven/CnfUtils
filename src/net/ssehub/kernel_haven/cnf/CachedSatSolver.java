package net.ssehub.kernel_haven.cnf;

import java.util.HashMap;
import java.util.Map;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A SAT solver that caches results.
 *
 * @author Adam
 */
public class CachedSatSolver extends SatSolver {

    private int cacheSize;
    
    private @NonNull Map<Cnf, Boolean> cache;
    
    /**
     * Creates a new {@link CachedSatSolver} with unlimited cache size.
     */
    public CachedSatSolver() {
        cache = new HashMap<>();
    }
    
    /**
     * Creates a new {@link CachedSatSolver} with the given base CNF (see {@link SatSolver#SatSolver(Cnf)} and unlimited
     * cache.
     * 
     * @param cnf The base CNF.
     */
    public CachedSatSolver(@NonNull Cnf cnf) {
        super(cnf);
        cache = new HashMap<>();
    }
    
    /**
     * Creates a {@link CachedSatSolver} with limited cache size.
     * 
     * @param cacheSize The maximum number of cache entries to store.
     */
    public CachedSatSolver(int cacheSize) {
        this();
        this.cacheSize = cacheSize;
    }
    

    /**
     * Creates a new {@link CachedSatSolver} with the given base CNF (see {@link SatSolver#SatSolver(Cnf)} and limited
     * cache size.
     * 
     * @param cnf The base CNF.
     * @param cacheSize The maximum number of cache entries to store.
     */
    public CachedSatSolver(@NonNull Cnf cnf, int cacheSize) {
        this(cnf);
        this.cacheSize = cacheSize;
    }
    
    @Override
    public boolean isSatisfiable(@NonNull Cnf cnf) throws SolverException {
        
        Boolean result = cache.get(cnf);
        
        if (result == null) {
            result = super.isSatisfiable(cnf);
            
            // TODO: maybe better remove older entries?
            if (cacheSize <= 0 || cache.size() < cacheSize) {
                cache.put(cnf, result);
            }
        }
        
        return result;
    }
    
}
