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

import java.util.HashMap;
import java.util.Map;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A SAT solver that caches results.
 *
 * @author Adam
 */
class CachedSatSolver implements ISatSolver {

    private int cacheSize;
    
    private @NonNull Map<Cnf, Boolean> cache;
    
    private @NonNull ISatSolver realSolver;
    
    /**
     * Creates a new {@link CachedSatSolver} with unlimited cache size.
     * 
     * @param realSolver The real solver to use.
     */
    public CachedSatSolver(@NonNull ISatSolver realSolver) {
        this.cache = new HashMap<>();
        this.realSolver = realSolver;
    }
    
    /**
     * Creates a {@link CachedSatSolver} with limited cache size.
     * 
     * @param realSolver The real solver to use.
     * @param cacheSize The maximum number of cache entries to store.
     */
    public CachedSatSolver(@NonNull ISatSolver realSolver, int cacheSize) {
        this(realSolver);
        this.cacheSize = cacheSize;
    }
    

    @Override
    public boolean isSatisfiable(@NonNull Cnf cnf) throws SolverException {
        
        Boolean result = cache.get(cnf);
        
        if (result == null) {
            result = realSolver.isSatisfiable(cnf);
            
            // TODO: maybe better remove older entries?
            if (cacheSize <= 0 || cache.size() < cacheSize) {
                cache.put(cnf, result);
            }
        }
        
        return result;
    }
    
}
