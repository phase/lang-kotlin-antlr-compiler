package xyz.jadonfowler.compiler.ast

import xyz.jadonfowler.compiler.ast.visitor.Visitor

class Program(val globalVariables: List<Variable>, val globalFunctions: List<Function>) {
    fun accept(visitor: Visitor) = visitor.visit(this)
}

class Function(val returnType: Type, val name: String, val formals: List<Formal>, val statements: List<Statement>) {
    fun accept(visitor: Visitor) = visitor.visit(this)
}

class Formal(val type: Type, val name: String) {
    fun accept(visitor: Visitor) = visitor.visit(this)
}

class Variable(val type: Type, val name: String) {
    fun accept(visitor: Visitor) = visitor.visit(this)
}


abstract class Type


abstract class Statement {
    open fun accept(visitor: Visitor) = visitor.visit(this)
}

open class Block(val statements: List<Statement>) : Statement() {
    override fun accept(visitor: Visitor) = visitor.visit(this)
}

/**
 * Block with an expression to be evaluated before the statements run
 *
 * <pre>
 *     (expr) {
 *          statements;
 *     }
 * </pre>
 *
 * @param exp Expression to test
 * @param block Statement to be run
 */
open class CheckedBlock(val exp: Expression, statements: List<Statement>) : Block(statements)

/**
 * If Statement
 * The statement list runs if the expression evaluates to true.
 * If the expression is false, control goes to elseStatement. This statement is an If Statement that has its own
 * expression to evaluate. There is no notion of an "Else Statement", they are If Statements with the expression set to
 * true.
 *
 * <pre>
 *     if (eA) {
 *         sA
 *     }
 *     else if (eB) {
 *         sB
 *     }
 *     else if (eC) {
 *         sC
 *     }
 *     else {
 *         sD
 *     }
 * </pre>
 *
 * This is represented in the tree as:
 *
 * <pre>
 *     IfStatement
 *     - eA
 *     - sA
 *     - IfStatement
 *       - eB
 *       - sB
 *       - IfStatement
 *         - eC
 *         - sC
 *         - IfStatement
 *           - true
 *           - sD
 * </pre>
 */
class IfStatement(exp: Expression, statements: List<Statement>, val elseStatement: IfStatement?) : CheckedBlock(exp, statements) {
    override fun accept(visitor: Visitor) = visitor.visit(this)
}

/**
 * elseStatement returns an IfStatement with the expression set to a TrueExpression
 * @param statements Statements to run
 */
fun elseStatement(statements: List<Statement>): IfStatement {
    return IfStatement(TrueExpression(), statements, null)
}

class WhileStatement(exp: Expression, statements: List<Statement>) : CheckedBlock(exp, statements) {
    override fun accept(visitor: Visitor) = visitor.visit(this)
}

abstract class Expression {
    open fun accept(visitor: Visitor) = visitor.visit(this)
}

class TrueExpression : Expression() {
    override fun accept(visitor: Visitor) = visitor.visit(this)
}

class FalseExpression : Expression() {
    override fun accept(visitor: Visitor) = visitor.visit(this)
}

class IntegerLiteral(val value: Int) : Expression() {
    override fun accept(visitor: Visitor) = visitor.visit(this)
}

class IdentifierExpression(val identifier: String) : Expression() {
    override fun accept(visitor: Visitor) = visitor.visit(this)
}


// Binary Operators
enum class Operator(val string: String) {
    // Maths
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),

    // Comparisons
    GREATER_THAN(">"),
    LESS_THAN("<"),
    EQUALS("=="),
    GREATER_THAN_EQUAL(">="),
    LESS_THAN_EQUAL("<="),
    AND("&&"),
    OR("||"),
}

class BinaryOperator(val expA: Expression, val operator: Operator, val expB: Expression) : Expression()
