package net.ssehub.kernel_haven.logic_utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.IFormulaVisitor;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * <p>
 * A utility class for finding groups of equal sub-trees in a {@link Formula}. Sub-trees are all {@link Formula}s
 * that are not simple {@link Variable}s, {@link True} or {@link False}. The formula itself is considered to be a
 * sub-tree, too. 
 * </p>
 * <p>
 * This class is not thread safe.
 * </p>
 *
 * @author Adam
 */
public class SubTreeGroupFinder {

    private @NonNull Map<Integer, List<@NonNull Formula>> groups;
    
    /**
     * Creates a new {@link SubTreeGroupFinder}.
     */
    public SubTreeGroupFinder() {
        groups = new HashMap<>();
    }
    
    /**
     * Finds all groups of structurally equal sub-trees inside the given formula. See the class comment for a definition
     * of sub-tree.
     * 
     * @param formula The formula to find all sub-trees in.
     * 
     * @return A list of all groups of structurally equal sub-trees.
     * 
     * @see FormulaStructureChecker#isStructurallyEqual(Formula, Formula)
     */
    public @NonNull List<@NonNull List<@NonNull Formula>> findGroups(@NonNull Formula formula) {
        formula.accept(new Visitor()); // this fills this.groups
        
        @SuppressWarnings("null")
        List<@NonNull List<@NonNull Formula>> result = new ArrayList<>(groups.values());
        
        groups.clear(); // for next round
        
        return result;
    }
    
    /**
     * Adds a sub-tree to the result map. This method will find the proper group to add the sub-tree to.
     * 
     * @param subTree The sub-tree to add.
     * @param hash The hash of the sub-tree.
     */
    private void addSubTree(@NonNull Formula subTree, int hash) {
        List<@NonNull Formula> group = groups.getOrDefault(hash, new LinkedList<>());
        groups.putIfAbsent(hash, group);
        
        boolean add = true;
        if (!group.isEmpty()) {
            // check for actual equality, to avoid hash collisions
            add = FormulaStructureChecker.isStructurallyEqual(group.get(0), subTree);
        }
        
        if (add) {
            group.add(subTree);
        } else {
            // detected hash collision -> add to next place
            addSubTree(subTree, hash * 23);
        }
    }
    
    /**
     * Visitor for finding all sub-trees in a formula. This will call
     * {@link SubTreeGroupFinder#addSubTree(Formula, int)} for all found sub-trees. Additionally, the visit methods
     * return the hash of the visited element. This way, calculation the hash of sub-trees is done in parallel while
     * visiting the {@link Formula}. This avoids having to call {@link Formula#hashCode()} all the time, and thus
     * avoids many recursive hashCode() evaluations.
     */
    private class Visitor implements IFormulaVisitor<@NonNull Integer> {

        @Override
        public @NonNull Integer visitFalse(@NonNull False falseConstant) {
            return falseConstant.hashCode();
        }

        @Override
        public @NonNull Integer visitTrue(@NonNull True trueConstant) {
            return trueConstant.hashCode();
        }

        @Override
        public @NonNull Integer visitVariable(@NonNull Variable variable) {
            return variable.hashCode();
        }

        @Override
        public @NonNull Integer visitNegation(@NonNull Negation formula) {
            int nestedHash = formula.getFormula().accept(this);
            
            int hash = nestedHash * 123;
            addSubTree(formula, hash);
            
            return hash;
        }

        @Override
        public @NonNull Integer visitDisjunction(@NonNull Disjunction formula) {
            int leftHash = formula.getLeft().accept(this);
            int rightHash = formula.getRight().accept(this);
            
            int hash = (leftHash + rightHash) * 213;
            addSubTree(formula, hash);
            
            return hash;
        }

        @Override
        public @NonNull Integer visitConjunction(@NonNull Conjunction formula) {
            int leftHash = formula.getLeft().accept(this);
            int rightHash = formula.getRight().accept(this);
            
            int hash = (leftHash + rightHash) * 4564;
            addSubTree(formula, hash);
            
            return hash;
        }
        
    }
    
}
