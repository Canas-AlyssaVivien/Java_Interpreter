enum TokenType {
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, COLON, SLASH, STAR, COMMENT, CONCAT, MOD,
    ESC1, ESC2,
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    IDENTIFIER, STRING, NUMBER, BOOL, CHAR, INT,
    AND, CLASS, ELSE, FALSE, FOR, IF, OR, NOT,
    DISPLAY, RETURN, TRUE, VAR, WHILE, SCAN,
    BEGIN, END, BEGIN_IF, BEGIN_WHILE, END_IF, END_WHILE, EOF, FLOAT, CODE, NLINE, SPACE    
}

class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
