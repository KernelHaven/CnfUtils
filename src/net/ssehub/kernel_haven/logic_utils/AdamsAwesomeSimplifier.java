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
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.util.PerformanceProbe;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.FormulaSimplifier;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Adam's awesome simplification heuristic. This heuristic to shorten {@link Formula}s performs the following steps:
 * <ol>
 *      <li>Prune all constants</li>
 *      <li>Move negations inwards (as much as possible)</li>
 *      <li>Call simple simplification ({@link LogicUtils#simplifyWithVisitor(Formula)})</li>
 *      <li>Call sub-tree based simplification ({@link SubTreeSimplifier})</li>
 *      <li>Move negations outwards (were possible)</li>
 *      <li>Call sub-tree based simplification ({@link SubTreeSimplifier})</li>
 * </ol>
 *
 * @author Adam
 */
public class AdamsAwesomeSimplifier {

    /**
     * Don't allow any instances.
     */
    private AdamsAwesomeSimplifier() {
    }
    
    /**
     * Moves all {@link Negation}s inwards as much as possible. After this, negations only occur around
     * {@link Variable}s, {@link True} and {@link False}.
     * 
     * @param formula The formula to move the negation inwards for.
     * 
     * @return The formula with moved negations.
     */
    private static @NonNull Formula moveNegationInwards(@NonNull Formula formula) {
        
        Formula result;
        
        if (formula instanceof Disjunction) {
            Disjunction dis = (Disjunction) formula;
            result = new Disjunction(moveNegationInwards(dis.getLeft()), moveNegationInwards(dis.getRight()));
            
        } else if (formula instanceof Conjunction) {
            Conjunction con = (Conjunction) formula;
            result = new Conjunction(moveNegationInwards(con.getLeft()), moveNegationInwards(con.getRight()));
            
        } else if (formula instanceof Negation) {
            Formula nested = ((Negation) formula).getFormula();
            
            if (nested instanceof Disjunction) {
                Disjunction dis = (Disjunction) nested;
                result = and(moveNegationInwards(not(dis.getLeft())), moveNegationInwards(not(dis.getRight())));
                
            } else if (nested instanceof Conjunction) {
                Conjunction con = (Conjunction) nested;
                result = or(moveNegationInwards(not(con.getLeft())), moveNegationInwards(not(con.getRight())));
                
            } else {
                result = formula;
            }
            
        } else {
            result = formula;
        }
        
        return result;
    }
    
    /**
     * Moves {@link Negation}s outwards, if possible. For example, this converts <code>!A || !B</code> to
     * <code>!(A && B)</code>.
     * 
     * @param formula The formula to move negations for.
     * 
     * @return The formula with moved negations.
     */
    private static @NonNull Formula moveNegationOutwards(@NonNull Formula formula) {
        Formula result;
        
        if (formula instanceof Disjunction) {
            Disjunction dis = (Disjunction) formula;
            List<@NonNull Formula> allTerms = new LinkedList<>();
            FormulaStructureChecker.getAllDisjunctionTerms(dis).stream()
                    .map((term) -> moveNegationOutwards(term))
                    .forEach(allTerms::add);
            
            boolean allNegated = true;
            for (Formula f : allTerms) {
                if (!(f instanceof Negation)) {
                    allNegated = false;
                    break;
                }
            }
            
            if (allNegated) {
                result = ((Negation) notNull(allTerms.remove(0))).getFormula();
                for (Formula f : allTerms) {
                    result = and(result, ((Negation) f).getFormula());
                }
                result = new Negation(result);
                
            } else {
                result = notNull(allTerms.remove(0));
                for (Formula term : allTerms) {
                    result = or(result, term);
                }
            }
            
        } else if (formula instanceof Conjunction) {
            Conjunction con = (Conjunction) formula;
            List<@NonNull Formula> allTerms = new LinkedList<>();
            FormulaStructureChecker.getAllConjunctionTerms(con).stream()
                    .map((term) -> moveNegationOutwards(term))
                    .forEach(allTerms::add);
            
            boolean allNegated = true;
            for (Formula f : allTerms) {
                if (!(f instanceof Negation)) {
                    allNegated = false;
                    break;
                }
            }
            
            if (allNegated) {
                result = ((Negation) notNull(allTerms.remove(0))).getFormula();
                for (Formula f : allTerms) {
                    result = or(result, ((Negation) f).getFormula());
                }
                result = new Negation(result);
                
            } else {
                result = notNull(allTerms.remove(0));
                for (Formula term : allTerms) {
                    result = and(result, term);
                }
            }
            
        } else if (formula instanceof Negation) {
            result = new Negation(moveNegationOutwards(((Negation) formula).getFormula()));
            
        } else {
            result = formula;
        }
        
        return result;
    }
    
    /**
     * Runs this simplification heuristic on the given formula.
     * 
     * @param formula The formula to simplify.
     * 
     * @return The simplified formula.
     */
    private static @NonNull Formula simplifyImpl(@NonNull Formula formula) {
        PerformanceProbe p;
        
        // Step 1: prune all constants
        p = new PerformanceProbe("AAS: 1) Prune Constants");
        formula = FormulaSimplifier.defaultSimplifier(formula);
        p.close();
        
        
        // Step 2: move all negations inwards
        p = new PerformanceProbe("AAS: 2) Move Negations Inward");
        formula = moveNegationInwards(formula);
        p.close();
        
        // Step 3.1: simple simplification
        p = new PerformanceProbe("AAS: 3.1) Simple Simplification");
        formula = LogicUtils.simplifyWithVisitor(formula);
        p.close();
        
        // Step 3.2: simple simplification
        // run the FormulaSimplificationVisitor2 standalone, as the SubTreeSimplifier does not call it for
        // the unmodified formula (i.e. no sub-trees replaced)
        p = new PerformanceProbe("AAS: 3.2) Simple Simplification");
        formula = new FormulaSimplificationVisitor2().visit(formula);
        p.close();
        
        // Step 4: sub-tree group simplifier
        p = new PerformanceProbe("AAS: 4) Sub-Tree Simplifier");
        formula = SubTreeSimplifier.simplify(formula);
        p.close();
        
        // Step 5: move negations outward again (where applicable)
        p = new PerformanceProbe("AAS: 5) Move Negations Outward");
        formula = moveNegationOutwards(formula);
        p.close();
        
        // Step 6: sub-tree group simplifier
        p = new PerformanceProbe("AAS: 6) Sub-Tree Simplifier");
        formula = SubTreeSimplifier.simplify(formula);
        p.close();
        
        return formula;
    }
    
    /**
     * Runs this simplification heuristic on the given formula.
     * 
     * @param formula The formula to simplify.
     * 
     * @return The simplified formula.
     */
    public static @NonNull Formula simplify(@NonNull Formula formula) {
        int shortestLength = formula.toString().length();
        Formula shortest = formula;
        
        int iteration = 0;
        
        int previousLength;
        int currentLength = formula.toString().length();
        do {
            PerformanceProbe p = new PerformanceProbe("AAS iteration " + (++iteration));
            
            previousLength = currentLength;
            formula = simplifyImpl(formula);
            currentLength = formula.toString().length();
            
            if (currentLength < shortestLength) {
                shortestLength = currentLength;
                shortest = formula;
            }

            p.addExtraData("Relative length", (double) currentLength / previousLength);
            p.close();
        } while (currentLength < previousLength);
        
        return shortest;
    }

}
