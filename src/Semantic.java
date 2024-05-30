import java.util.List;
import java.util.Scanner;

public class Semantic implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();
    private final Scanner scanner = new Scanner(System.in);
    private boolean displayExecuted = false;

    @Override
public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
        case GREATER:
            checkNumberOperands(expr.operator, left, right);
            return (double) left > (double) right;
        case GREATER_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double) left >= (double) right;
        case LESS:
            checkNumberOperands(expr.operator, left, right);
            return (double) left < (double) right;
        case LESS_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double) left <= (double) right;
        case MINUS:
            checkNumberOperands(expr.operator, left, right);
            return (double) left - (double) right;
        case PLUS:
            if (left instanceof Double && right instanceof Double) {
                return (double) left + (double) right;
            }
            if (left instanceof String && right instanceof String) {
                return (String) left + (String) right;
            }
            throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
        case SLASH:
            checkNumberOperands(expr.operator, left, right);
            return (double) left / (double) right;
        case STAR:
            checkNumberOperands(expr.operator, left, right);
            return (double) left * (double) right;
        case MOD:
            checkNumberOperands(expr.operator, left, right);
            return (double) left % (double) right;
        case BANG_EQUAL:
            return !isEqual(left, right);
        case EQUAL_EQUAL:
            return isEqual(left, right);
    }
    // Unreachable.
    return null;
}


    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }
        // Unreachable.
        return null;
    }

    private void checkNumberOperands(Token operator,
        Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }
    
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    void execute(Stmt stmt) {
        if (stmt != null) {
            stmt.accept(this);
        }
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
            execute(statement);
            }
            if (!displayExecuted) {
                System.out.println("No Error");
            }
        } catch (RuntimeError error) {
            System.err.println(error.getMessage());
        }
    }

    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
        return text;
        }
        return object.toString();
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitDISPLAYStmt(Stmt.DISPLAY stmt) {
    displayExecuted = true; 
    StringBuilder output = new StringBuilder();
    for (Expr expression : stmt.expressions) {
        Object value = evaluate(expression);
        if (value instanceof String) {
            String str = (String) value;
            if (str.equals("$")) {
                System.out.println(); 
            } else {
                output.append(str);
            }
        } else if (value instanceof Expr.EscapeCode) {
            handleEscapeCode((Expr.EscapeCode) value, output);
        } else {
            output.append(stringify(value));
        }
    }
    System.out.print(output.toString());
    return null;
}

private void handleEscapeCode(Expr.EscapeCode escapeCode, StringBuilder output) {
    char code = escapeCode.code;
    switch (code) {
        case '$':
            output.append(System.lineSeparator());
            break;
        case '[':
            output.append('[');
            break;
        case ']':
            output.append(']'); 
            break;
        default:
            break;
    }
}

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
    for (int i = 0; i < stmt.names.size(); i++) {
        Object value = null;
        if (stmt.initializers.get(i) != null) {
            value = evaluate(stmt.initializers.get(i));
        }
        environment.define(stmt.names.get(i).lexeme, value, stmt.type.type);
    }
    return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        Object value = environment.get(expr.name);
        return value;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
    }

    @Override
    public Void visitBeginCodeStmt(Stmt.BeginCode stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
            execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitIFStmt(Stmt.IF stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
    execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
    execute(stmt.elseBranch);
    }
    return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
    Object left = evaluate(expr.left);
    if (expr.operator.type == TokenType.OR) {
    if (isTruthy(left)) return left;
    } else {
    if (!isTruthy(left)) return left;
    }
    return evaluate(expr.right);
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
    execute(stmt.body);
    }
    return null;
    }

    @Override
    public Void visitSCANStmt(Stmt.SCAN stmt) {
        for (Token name : stmt.names) {
            TokenType type = environment.getVarType(name.lexeme);
            String input = scanner.nextLine();
            Object value;
    
            switch (type) {
                case INT:
                    value = Double.parseDouble(input); // Correctly parse as Integer
                    break;
                case BOOL:
                    value = Boolean.parseBoolean(input);
                    break;
                case CHAR:
                    value = input.charAt(0);
                    break;
                case FLOAT:
                    value = Double.parseDouble(input);
                    break;
                default:
                    throw new RuntimeError(name, "Unsupported variable type.");
            }
    
            environment.assign(name, value);
        }
        return null;
    }
    


    @Override
    public Object visitConcatenationExpr(Expr.Concatenation expr) {
        throw new UnsupportedOperationException("Unimplemented method 'visitConcatenationExpr'");
    }

    @Override
    public Object visitEscapeCodeExpr(Expr.EscapeCode expr) {
        throw new UnsupportedOperationException("Unimplemented method 'visitEscapeCodeExpr'");
    }
 }

