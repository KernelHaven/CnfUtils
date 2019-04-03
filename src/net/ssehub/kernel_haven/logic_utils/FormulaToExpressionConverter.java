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

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Literal;
import com.bpodgursky.jbool_expressions.Not;
import com.bpodgursky.jbool_expressions.Or;

import net.ssehub.kernel_haven.util.FormatException;
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
 * Converter class between KernelHavens {@link Formula} and JBool_Expressions {@link Expression}.
 *  
 * @author Adam
 * @author El-Sharkawy
 */
class FormulaToExpressionConverter implements IFormulaVisitor<@NonNull Expression<String>> {
    private Map<String, Variable> varMapping = new HashMap<>();

    @Override
    public @NonNull Expression<String> visitFalse(@NonNull False falseConstant) {
        return notNull(Literal.getFalse());
    }

    @Override
    public @NonNull Expression<String> visitTrue(@NonNull True trueConstant) {
        return notNull(Literal.getTrue());
    }

    @Override
    public @NonNull Expression<String> visitVariable(@NonNull Variable variable) {
        varMapping.put(variable.getName(), variable);
        return notNull(com.bpodgursky.jbool_expressions.Variable.of(variable.getName()));
    }

    @Override
    public @NonNull Expression<String> visitNegation(@NonNull Negation formula) {
        return notNull(Not.of(visit(formula.getFormula())));
    }

    @Override
    public @NonNull Expression<String> visitDisjunction(@NonNull Disjunction formula) {
        return notNull(Or.of(visit(formula.getLeft()), visit(formula.getRight())));
    }

    @Override
    public @NonNull Expression<String> visitConjunction(@NonNull Conjunction formula) {
        return notNull(And.of(visit(formula.getLeft()), visit(formula.getRight())));
    }
    
    /**
     * Converts the given {@link Expression} back into a {@link Formula}.
     * 
     * @param expr The expression to convert. Must not be <code>null</code>.
     * 
     * @return The {@link Formula} that was created from the given expression. Not <code>null</code>.
     * @throws FormatException If the formula could not be parsed correctly.
     */
    public @NonNull Formula expressionToFormula(@NonNull Expression<String> expr) throws FormatException {
        Formula result = null;
        
        if (expr instanceof Literal) {
            result = (((Literal<String>) expr).getValue()) ? True.INSTANCE : False.INSTANCE;
            
        } else if (expr instanceof Or) {
            result = translateOrExpression((Or<String>) expr);
        } else if (expr instanceof And) {
            result = translateAndExpression((And<String>) expr);
        } else if (expr instanceof Not) {
            result = new Negation(expressionToFormula(notNull(((Not<String>) expr).getE())));
        } else if (expr instanceof com.bpodgursky.jbool_expressions.Variable) {
            String varName = notNull(((com.bpodgursky.jbool_expressions.Variable<String>) expr).getValue());
            result = varMapping.get(varName);
            if (null == result) {
                // Should not occur, except in tests. However, this is also a fallback.
                result = new Variable(varName);
            }
        } else {
            throw new FormatException("Could not parse \"" + expr + "\", due an unexpected element of type: "
                + expr.getClass());
        }
        
        return result;
    }

    /**
     * Part of the {@link #expressionToFormula(Expression)} method to translate OR expressions.
     * @param expr An OR expression to translate.
     * @return The translated formula.
     * @throws FormatException If the formula could not be parsed correctly.
     */
    private @NonNull Formula translateOrExpression(@NonNull Or<String> expr) throws FormatException {
        Formula result;
        @SuppressWarnings("null") // children are never null
        List<@NonNull Expression<String>> children = expr.getChildren();
        if (children.size() < 1) {
            result = True.INSTANCE;
        } else if (children.size() == 1) {
            result = expressionToFormula(children.get(0));
        } else if (children.size() == 2 ) {
            // Special case: 2 elements can directly be translated (safe memory instead of using generic approach)
            result = new Disjunction(expressionToFormula(children.get(0)), expressionToFormula(children.get(1)));
        } else {
            /*
             *  jbool_expressions allows OR and AND expressions with more than two elements
             *  -> Try to keep the tree as flat as possible
             */
            Queue<@NonNull Formula> translatedElements = new ArrayDeque<>();
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
            result = notNull(translatedElements.poll());
        }
        
        return result;
    }
    
    /**
     * Part of the {@link #expressionToFormula(Expression)} method to translate AND expressions.
     * @param expr An OR expression to translate.
     * @return The translated formula.
     * @throws FormatException If the formula could not be parsed correctly.
     */
    private @NonNull Formula translateAndExpression(@NonNull And<String> expr) throws FormatException {
        Formula result;
        @SuppressWarnings("null") // children are never null
        List<@NonNull Expression<String>> children = expr.getChildren();
        if (children.size() < 1) {
            result = True.INSTANCE;
        } else if (children.size() == 1) {
            result = expressionToFormula(children.get(0));
        } else if (children.size() == 2 ) {
            // Special case: 2 elements can directly be translated (safe memory instead of using generic approach)
            result = new Conjunction(expressionToFormula(children.get(0)), expressionToFormula(children.get(1)));
        } else {
            /*
             *  jbool_expressions allows OR and AND expressions with more than two elements
             *  -> Try to keep the tree as flat as possible
             */
            Queue<@NonNull Formula> translatedElements = new ArrayDeque<>();
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
            result = notNull(translatedElements.poll());
        }
        
        return result;
    }

}
