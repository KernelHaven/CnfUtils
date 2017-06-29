package net.ssehub.kernel_haven.cnf;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * A CNF converter based on https://www.cs.jhu.edu/~jason/tutorials/convert-to-CNF.html
 * 
 * Constants in the boolean {@link Formula} are converted to {@link #PSEUDO_TRUE} and {@link #PSEUDO_TRUE}.
 * 
 * @author Adam (copied from KernelMiner project)
 * @author Johannes
 */
public class RecursiveCnfConverter implements IFormulaToCnfConverter {
    
    private static final Variable PSEUDO_TRUE = new Variable("PSEUDO_TRUE");
    private static final Variable PSEUDO_FALSE = new Variable("PSEUDO_FALSE");
    
    @Override
    public Cnf convert(Formula formula) throws ConverterException {
        Cnf result = new Cnf();
        
        if (containsConstants(formula)) {
            result.addRow(new CnfVariable(true, PSEUDO_FALSE.getName()));
            result.addRow(new CnfVariable(false, PSEUDO_TRUE.getName()));
            
            formula = replaceConstants(formula);
        }
        
        result = result.combine(convertPrivate(formula));
        
        return result;
    }
    
    /**
     * Checks if the boolean formula contains the constants true or false.
     * 
     * @param tree The formula to check.
     * @return Whether the formula contains constants.
     */
    private static boolean containsConstants(Formula tree) {
        boolean result = false;
        
        if (tree instanceof True || tree instanceof False) {
            result = true;
            
        } else if (tree instanceof Disjunction) {
            result = containsConstants(((Disjunction) tree).getLeft())
                    || containsConstants(((Disjunction) tree).getRight());
            
        } else if (tree instanceof Conjunction) {
            result = containsConstants(((Conjunction) tree).getLeft())
                    || containsConstants(((Conjunction) tree).getRight());
            
        } else if (tree instanceof Negation) {
            result = containsConstants(((Negation) tree).getFormula());
        }
        
        return result;
    }
    
    /**
     * Replaces all constants with PSEUDO_TRUE or PSEUDO_FALSE.
     * 
     * @param tree The formula to replace the constants in.
     * @return A copy of the formula with the constants replaced.
     * @throws ConverterException If unexpected elements are found in the tree.
     */
    private static Formula replaceConstants(Formula tree) throws ConverterException {
        Formula result = null;
        
        
        if (tree instanceof True) {
            result = PSEUDO_TRUE;
            
        } else if (tree instanceof False) {
            result = PSEUDO_FALSE;
            
        } else if (tree instanceof Disjunction) {
            result = new Disjunction(
                    replaceConstants(((Disjunction) tree).getLeft()),
                    replaceConstants(((Disjunction) tree).getRight()));
            
        } else if (tree instanceof Conjunction) {
            result = new Conjunction(
                    replaceConstants(((Conjunction) tree).getLeft()),
                    replaceConstants(((Conjunction) tree).getRight()));
            
        } else if (tree instanceof Negation) {
            result = new Negation(replaceConstants(((Negation) tree).getFormula()));
            
        } else if (tree instanceof Variable) {
            result = tree;
            
        } else {
            throw new ConverterException("Unexpected element found in tree: " + tree.getClass());
        }
        
        return result;
    }
    
    /**
     * Internal convert method. Recursively called to parse parts of the tree.
     * 
     * @param tree The formula to convert.
     * @return The CNF representing the formula.
     * @throws ConverterException If an unexpected element is found in the tree.
     */
    protected final Cnf convertPrivate(Formula tree) throws ConverterException {
        /*
         * See https://www.cs.jhu.edu/~jason/tutorials/convert-to-CNF.html
         */
        Cnf result = null;
        
        if (tree instanceof Variable) {
            result = handleVariable((Variable) tree);
            
        } else if (tree instanceof Disjunction) {
            result = handleOr((Disjunction) tree);
            
        } else if (tree instanceof Conjunction) {
            result = handleAnd((Conjunction) tree);
            
        } else if (tree instanceof Negation) {
            result = handleNot((Negation) tree);
        } else {
            throw new ConverterException("Invalid element in tree: " + tree.getClass());
        }
        
        return result;
    }
    
    /**
     * Converts a variable to CNF.
     * 
     * @param var The variable to convert.
     * @return The resulting CNF representing the variable.
     * @throws ConverterException If an unexpected element is found in the tree.
     */
    protected Cnf handleVariable(Variable var) throws ConverterException {
        Cnf result = new Cnf();
        result.addRow(new CnfVariable(false, var.getName()));
        return result;
    }
    
    /**
     * Converts a disjunction to CNF.
     * 
     * @param call The disjunction to convert.
     * @return The resulting CNF representing the disjunction.
     * @throws ConverterException If an unexpected element is found in the tree.
     */
    protected Cnf handleOr(Disjunction call) throws ConverterException {
        /*
         * We have call = P v Q
         * 
         * CONVERT(P) must have the form P1 ^ P2 ^ ... ^ Pm, and
         * CONVERT(Q) must have the form Q1 ^ Q2 ^ ... ^ Qn,
         * where all the Pi and Qi are dijunctions of literals.
         * So we need a CNF formula equivalent to
         *    (P1 ^ P2 ^ ... ^ Pm) v (Q1 ^ Q2 ^ ... ^ Qn).
         * So return (P1 v Q1) ^ (P1 v Q2) ^ ... ^ (P1 v Qn)
         *         ^ (P2 v Q1) ^ (P2 v Q2) ^ ... ^ (P2 v Qn)
         *           ...
         *         ^ (Pm v Q1) ^ (Pm v Q2) ^ ... ^ (Pm v Qn)
         */
        
        Cnf result = new Cnf();
        
        Cnf leftSide = convertPrivate(call.getLeft());
        Cnf rightSide = convertPrivate(call.getRight());
        
        for (List<CnfVariable> p : leftSide.getTable()) {
            for (List<CnfVariable> q : rightSide.getTable()) {
                
                List<CnfVariable> row = new ArrayList<>(p.size() + q.size());
                row.addAll(p);
                row.addAll(q);
                
                result.addRow(row.toArray(new CnfVariable[0]));
            }
        }
        
        return result;
    }
    
    /**
     * Converts a conjunction to CNF.
     * 
     * @param call The conjunction to convert.
     * @return The resulting CNF representing the conjunction.
     * @throws ConverterException If an unexpected element is found in the tree.
     */
    protected Cnf handleAnd(Conjunction call) throws ConverterException {
        /*
         * We have call = P ^ Q
         * 
         * CONVERT(P) must have the form P1 ^ P2 ^ ... ^ Pm, and
         * CONVERT(Q) must have the form Q1 ^ Q2 ^ ... ^ Qn,
         * where all the Pi and Qi are disjunctions of literals.
         * So return P1 ^ P2 ^ ... ^ Pm ^ Q1 ^ Q2 ^ ... ^ Qn.
         */
        
        Cnf leftSide = convertPrivate(call.getLeft());
        Cnf rightSide = convertPrivate(call.getRight());
        
        return leftSide.combine(rightSide);
    }
    
    /**
     * Converts a negation to CNF.
     * 
     * @param call The negation to convert.
     * @return The resulting CNF representing the negation.
     * @throws ConverterException If an unexpected element is found in the tree.
     */
    protected Cnf handleNot(Negation call) throws ConverterException {
        Cnf result = null;
        
        if (call.getFormula() instanceof Variable) {
            // If call has the form ~A for some variable A, then return call.
            Variable var = (Variable) call.getFormula();
            result = new Cnf();
            result.addRow(new CnfVariable(true, var.getName()));
            
        } else if (call.getFormula() instanceof Negation) {
            // If call has the form ~(~P), then return CONVERT(P). (double negation)
            
            Negation child = (Negation) call.getFormula();
            result = convertPrivate(child.getFormula());
            
        } else if (call.getFormula() instanceof Disjunction) {
            // If call has the form ~(P v Q), then return CONVERT(~P ^ ~Q). (de Morgan's Law)
            
            Disjunction innerCall = (Disjunction) call.getFormula();
            
            Formula p = innerCall.getLeft();
            Formula q = innerCall.getRight();
              
            Formula notP = new Negation(p);
            Formula notQ = new Negation(q);
            Formula newAnd = new Conjunction(notP, notQ);
            
            result = convertPrivate(newAnd);
            
        } else if (call.getFormula() instanceof Conjunction) {
            // If call has the form ~(P ^ Q), then return CONVERT(~P v ~Q). (de Morgan's Law)
            Conjunction innerCall = (Conjunction) call.getFormula();
            
            Formula p = innerCall.getLeft();
            Formula q = innerCall.getRight();
              
            Formula notP = new Negation(p);
            Formula notQ = new Negation(q);
            Formula newOr = new Disjunction(notP, notQ);

            result = convertPrivate(newOr);
        
        } else {
            throw new ConverterException("Invalid element in not call: " + call.getFormula().getClass());
        }
        
        return result;
    }

}
