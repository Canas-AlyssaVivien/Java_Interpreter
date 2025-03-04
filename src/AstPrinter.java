class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,
        expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (Expr expr : exprs) {
        builder.append(" ");
        builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
        }
        
        public static void main(String[] args) {
            Expr expression = new Expr.Binary(
            new Expr.Unary(
            new Token(TokenType.MINUS, "-", null, 1),
            new Expr.Literal(123)),
            new Token(TokenType.STAR, "*", null, 1),
            new Expr.Grouping(
            new Expr.Literal(45.67)));
            System.out.println(new AstPrinter().print(expression));
        }

        @Override
        public String visitVariableExpr(Expr.Variable expr) {
            throw new UnsupportedOperationException("Unimplemented method 'visitVariableExpr'");
        }

        @Override
        public String visitAssignExpr(Expr.Assign expr) {
            throw new UnsupportedOperationException("Unimplemented method 'visitAssignExpr'");
        }

        @Override
        public String visitLogicalExpr(Expr.Logical expr) {
            throw new UnsupportedOperationException("Unimplemented method 'visitLogicalExpr'");
        }

        @Override
        public String visitConcatenationExpr(Expr.Concatenation expr) {
            throw new UnsupportedOperationException("Unimplemented method 'visitConcatenationExpr'");
        }

        @Override
        public String visitEscapeCodeExpr(Expr.EscapeCode expr) {
            throw new UnsupportedOperationException("Unimplemented method 'visitEscapeCodeExpr'");
        }
}

