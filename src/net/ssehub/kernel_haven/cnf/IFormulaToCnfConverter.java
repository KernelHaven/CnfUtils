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

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A converter to convert boolean {@link Formula}s to {@link Cnf}.
 * 
 * @author Adam
 * @author Johannes
 */
public interface IFormulaToCnfConverter {
    
    /**
     * Converts the given boolean formula to CNF.
     * 
     * @param formula The formula to convert. Must not be <code>null</code>.
     * @return The CNF representing the formula. Never <code>null</code>.
     * 
     * @throws ConverterException If the conversion fails.
     */
    public @NonNull Cnf convert(@NonNull Formula formula) throws ConverterException;

}
