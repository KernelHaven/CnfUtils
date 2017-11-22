package net.ssehub.kernel_haven.logic_utils;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Literal;
import com.bpodgursky.jbool_expressions.Not;
import com.bpodgursky.jbool_expressions.Or;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.IFormulaVisitor;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Converter class between KernelHavens {@link Formula} and JBool_Expressions {@link Expression}.
 *  
 * @author Adam
 */
class FormulaToExpressionConverter implements IFormulaVisitor<Expression<String>> {

    @Override
    public Expression<String> visitFalse(False falseConstant) {
        return Literal.getFalse();
    }

    @Override
    public Expression<String> visitTrue(True trueConstant) {
        return Literal.getTrue();
    }

    @Override
    public Expression<String> visitVariable(Variable variable) {
        return com.bpodgursky.jbool_expressions.Variable.of(variable.getName());
    }

    @Override
    public Expression<String> visitNegation(Negation formula) {
        return Not.of(visit(formula.getFormula()));
    }

    @Override
    public Expression<String> visitDisjunction(Disjunction formula) {
        return Or.of(visit(formula.getLeft()), visit(formula.getRight()));
    }

    @Override
    public Expression<String> visitConjunction(Conjunction formula) {
        return And.of(visit(formula.getLeft()), visit(formula.getRight()));
    }
    
    /**
     * Converts the given {@link Expression} back into a {@link Formula}.
     * 
     * @param expr The expression to convert. Must not be <code>null</code>.
     * 
     * @return The {@link Formula} that was created from the given expression. Not <code>null</code>.
     */
    public static Formula expressionToFormula(Expression<String> expr) {
        Formula result = null;
        
        if (expr instanceof Literal) {
            result = (((Literal<String>) expr).getValue()) ? True.INSTANCE : False.INSTANCE;
            
        } else if (expr instanceof Or) {
            result = translateOrExpression((Or<String>) expr);
        } else if (expr instanceof And) {
            result = translateAndExpression((And<String>) expr);
        } else if (expr instanceof Not) {
            result = new Negation(expressionToFormula(((Not<String>) expr).getE()));
            
        } else if (expr instanceof com.bpodgursky.jbool_expressions.Variable) {
            result = new Variable(((com.bpodgursky.jbool_expressions.Variable<String>) expr).getValue());
            
        } else {
            throw new RuntimeException("TODO"); // TODO
        }
        
        return result;
    }

    /**
     * Part of the {@link #expressionToFormula(Expression)} method to translate OR expressions.
     * @param expr An OR expression to translate.
     * @return The translated formula.
     */
    private static Formula translateOrExpression(Or<String> expr) {
        Formula result;
        List<Expression<String>> children = expr.getChildren();
        if (children.size() <= 1) {
            throw new RuntimeException("TODO"); // TODO                
        } else if (children.size() == 2 ) {
            // Special case: 2 elements can directly be translated (safe memory instead of using generic approach)
            result = new Disjunction(expressionToFormula(children.get(0)), expressionToFormula(children.get(1)));
        } else {
            /*
             *  jbool_expressions allows OR and AND expressions with more than two elements
             *  -> Try to keep the tree as flat as possible
             */
            Queue<Formula> translatedElements = new ArrayDeque<>();
            Formula lastElement = null;
            for (int i = 0; i < children.size(); i++) {
                Formula translatedChild = expressionToFormula(children.get(i));                    
                if (null == lastElement) {
                    lastElement = translatedChild;
                } else {
                    translatedElements.add(new Disjunction(lastElement, translatedChild));
                    lastElement = null;
                }
            }
            // Consider situations with an odd number of elements
            if (null != lastElement) {
                translatedElements.add(lastElement);
            }
            // Create binary tree, as balanced as possible
            while (translatedElements.size() > 1) {
                translatedElements.add(new Disjunction(translatedElements.poll(), translatedElements.poll()));
            }
            result = translatedElements.poll();
        }
        
        return result;
    }
    
    /**
     * Part of the {@link #expressionToFormula(Expression)} method to translate AND expressions.
     * @param expr An OR expression to translate.
     * @return The translated formula.
     */
    private static Formula translateAndExpression(And<String> expr) {
        Formula result;
        List<Expression<String>> children = expr.getChildren();
        if (children.size() <= 1) {
            throw new RuntimeException("TODO"); // TODO                
        } else if (children.size() == 2 ) {
            // Special case: 2 elements can directly be translated (safe memory instead of using generic approach)
            result = new Conjunction(expressionToFormula(children.get(0)), expressionToFormula(children.get(1)));
        } else {
            /*
             *  jbool_expressions allows OR and AND expressions with more than two elements
             *  -> Try to keep the tree as flat as possible
             */
            Queue<Formula> translatedElements = new ArrayDeque<>();
            Formula lastElement = null;
            for (int i = 0; i < children.size(); i++) {
                Formula translatedChild = expressionToFormula(children.get(i));                    
                if (null == lastElement) {
                    lastElement = translatedChild;
                } else {
                    translatedElements.add(new Conjunction(lastElement, translatedChild));
                    lastElement = null;
                }
            }
            // Consider situations with an odd number of elements
            if (null != lastElement) {
                translatedElements.add(lastElement);
            }
            // Create binary tree, as balanced as possible
            while (translatedElements.size() > 1) {
                translatedElements.add(new Conjunction(translatedElements.poll(), translatedElements.poll()));
            }
            result = translatedElements.poll();
        }
        
        return result;
    }

}