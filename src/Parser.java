import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }
    
    private Expr expression() {
        return equality();
    }
    
    private Stmt statement() {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.DISPLAY)) return printStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.SCAN)) return scanStatement();
        if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
        if (match(TokenType.IDENTIFIER)) return assignmentStatement();
        return expressionStatement();
    }

    private Stmt assignmentStatement() {
        Token name = previous();
        if (match(TokenType.EQUAL)) {
            Expr value = expression();
            if (value == null) {
            throw error(peek(), "Expect expression after '='.");
        }
            return new Stmt.Expression(new Expr.Assign(name, value));
        }
        throw error(peek(), "Expect '=' after variable name.");
    }
    private Stmt declaration() {
        try {
        if (match(TokenType.INT) || match(TokenType.BOOL) || match(TokenType.CHAR) || match(TokenType.FLOAT)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt parseCodeBlock() {
        if (match(TokenType.BEGIN)) {
            List<Stmt> statements = new ArrayList<>();
            while (!check(TokenType.END) && !isAtEnd()) {
                statements.add(declaration());
            }
            consume(TokenType.END, "Program must end with END CODE.");
            return new Stmt.Block(statements);
        } else {
            consume(TokenType.BEGIN, "Expect BEGIN CODE to start program.");
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token type = previous();
        List<Token> names = new ArrayList<>();
        List<Expr> initializers = new ArrayList<>();

        do {
            Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
            names.add(name);

            Expr initializer = null;
            if (match(TokenType.EQUAL)) {
                initializer = expression();
                ensureTypeCompatibility(type, initializer);
            } else {
                if (type.type == TokenType.FLOAT) {
                    initializer = new Expr.Literal(0.0);
                } else {
                    initializer = new Expr.Literal(0);
                }
            }
            initializers.add(initializer);
        } while (match(TokenType.COMMA));
        
        return new Stmt.Var(type, names, initializers);
    }
    
    private void ensureTypeCompatibility(Token type, Expr initializer) {
        if (initializer instanceof Expr.Literal) {
            Object value = ((Expr.Literal) initializer).value;
            if (type.type == TokenType.INT && !(value instanceof Double)) {
                throw error(type, "Type mismatch: expected INT.");
            }
            if (type.type == TokenType.BOOL && !(value instanceof Boolean)) {
                throw error(type, "Type mismatch: expected BOOL.");
            }
            if (type.type == TokenType.CHAR && !(value instanceof Character)) {
                throw error(type, "Type mismatch: expected CHAR.");
            }
            if (type.type == TokenType.FLOAT && !(value instanceof Double)) {
                throw error(type, "Type mismatch: expected FLOAT.");
            }
        } else if (initializer instanceof Expr.Variable) {
            
        }
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }
        
    private Token peek() {
        return tokens.get(current);
    }
        
    private Token previous() {
        return tokens.get(current - 1);
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL, TokenType.BANG_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR, TokenType.MOD)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }
    
    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NUMBER)) {
            return new Expr.Literal(Double.parseDouble(previous().lexeme));
        }
        if (match(TokenType.STRING)) {
            String str = previous().literal.toString();
            if (str.equals("TRUE")) {
                return new Expr.Literal(true);
            } else if (str.equals("FALSE")) {
                return new Expr.Literal(false);
            } else {
                return new Expr.Literal(str); 
            } 
        }

        if (match(TokenType.CHAR)) {    
            return new Expr.Literal(previous().literal); 
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
            }

        if (match(TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH, TokenType.MOD)) {
                Token operator = previous();
                Expr right = primary();
                return new Expr.Binary(new Expr.Literal(0), operator, right); 
        }
        
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if (match(TokenType.NLINE)) {
            return new Expr.Literal("$");
        }
    
        if (match(TokenType.CONCAT)) {
            Expr left = primary();
            Token operator = previous();
            Expr right = primary();
            return new Expr.Concatenation(left, operator, right);
        }
    
        if (match(TokenType.ESC1)) {
            Token charToken = consume(TokenType.CHAR, "Expect character inside '[' and ']'.");
            consume(TokenType.ESC2, "Expect ']' after character.");
            return new Expr.EscapeCode(charToken.lexeme.charAt(0));
        }
        

        throw error(peek(), "Expect expression.");
    }
    

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }


    private void synchronize() {
        advance();
        while (!isAtEnd()) {
    
        switch (peek().type) {
        case FOR:
        case IF:
        case WHILE:
        case DISPLAY:
        case SCAN:
        case INT:
        case CHAR:
        case BOOL:
            return;
        }
        advance();
        }
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(parseCodeBlock());
        }
        return statements;
    }


    private Stmt printStatement() {
        List<Expr> expressions = new ArrayList<>();
        consume(TokenType.COLON, "Expect ':' after 'DISPLAY'.");

        do {
            expressions.add(expression());
        } while (match(TokenType.CONCAT) || match(TokenType.PLUS) || match(TokenType.STAR) || match(TokenType.MINUS) || match(TokenType.SLASH) || match(TokenType.MOD));

        return new Stmt.DISPLAY(expressions);
    }
    
    private Stmt scanStatement() {
        consume(TokenType.COLON, "Expect ':' after 'SCAN'.");
        List<Token> names = new ArrayList<>();
    
        do {
            Token name = consume(TokenType.IDENTIFIER, "Expect variable name after 'SCAN:'.");
            names.add(name);
        } while (match(TokenType.COMMA));
    
        return new Stmt.SCAN(names);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.BEGIN_IF) && !isAtEnd()) {
        statements.add(declaration());
        }
        consume(TokenType.END_IF, "Expect '}' after block.");
        return statements;
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");
    
        consume(TokenType.BEGIN_IF, "Expect 'BEGIN IF' to start the if-block.");
        List<Stmt> thenStatements = new ArrayList<>();
        while (!check(TokenType.END_IF) && !isAtEnd()) {
            thenStatements.add(declaration());
        }
        consume(TokenType.END_IF, "Expect 'END IF' after if-block.");
        Stmt thenBranch = new Stmt.Block(thenStatements);
    
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            if (match(TokenType.IF)) {
                elseBranch = elseIfStatement();
            } else {
                consume(TokenType.BEGIN_IF, "Expect 'BEGIN IF' to start the else-block.");
                List<Stmt> elseStatements = new ArrayList<>();
                while (!check(TokenType.END_IF) && !isAtEnd()) {
                    elseStatements.add(declaration());
                }
                consume(TokenType.END_IF, "Expect 'END IF' after else-block.");
                elseBranch = new Stmt.Block(elseStatements);
            }
        }
        return new Stmt.IF(condition, thenBranch, elseBranch);
    }
    
    private Stmt elseIfStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'else if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after else if condition.");
    
        consume(TokenType.BEGIN_IF, "Expect 'BEGIN IF' to start the else if-block.");
        List<Stmt> elseIfStatements = new ArrayList<>();
        while (!check(TokenType.END_IF) && !isAtEnd()) {
            elseIfStatements.add(declaration());
        }
        consume(TokenType.END_IF, "Expect 'END IF' after else if-block.");
        Stmt elseIfBranch = new Stmt.Block(elseIfStatements);
    
        Stmt nextElseBranch = null;
        if (match(TokenType.ELSE)) {
            if (match(TokenType.IF)) {
                nextElseBranch = elseIfStatement();
            } else {
                consume(TokenType.BEGIN_IF, "Expect 'BEGIN IF' to start the else-block.");
                List<Stmt> elseStatements = new ArrayList<>();
                while (!check(TokenType.END_IF) && !isAtEnd()) {
                    elseStatements.add(declaration());
                }
                consume(TokenType.END_IF, "Expect 'END IF' after else-block.");
                nextElseBranch = new Stmt.Block(elseStatements);
            }
        }
    
        return new Stmt.IF(condition, elseIfBranch, nextElseBranch);
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'WHILE'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after while condition.");
    
        consume(TokenType.BEGIN_WHILE, "Expect 'BEGIN WHILE' to start the while-block.");
        List<Stmt> bodyStatements = new ArrayList<>();
        while (!check(TokenType.END_WHILE) && !isAtEnd()) {
            bodyStatements.add(declaration());
        }
        consume(TokenType.END_WHILE, "Expect 'END WHILE' after while-block.");
        Stmt body = new Stmt.Block(bodyStatements);
    
        return new Stmt.While(condition, body);
    }    
       
}
