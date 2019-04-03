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

import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Thrown by converters for boolean formulas if they fail.
 * 
 * @author Adam
 * @author Johannes
 */
public class ConverterException extends Exception {

    private static final long serialVersionUID = -2153432140783061540L;

    /**
     * Creates a new {@link ConverterException} with the given message.
     * 
     * @param message The message to display.
     */
    public ConverterException(@Nullable String message) {
        super(message);
    }
    
    /**
     * Creates a new {@link ConverterException} with the given cause.
     * 
     * @param cause The cause of this exception.
     */
    public ConverterException(@Nullable Throwable cause) {
        super(cause);
    }
    
    /**
     * Creates a new {@link ConverterException} with the given message and cause.
     * 
     * @param message The message to display.
     * @param cause The cause of this exception.
     */
    public ConverterException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
    
}
