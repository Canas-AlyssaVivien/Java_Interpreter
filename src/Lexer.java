import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;
    private int start = 0;
    private int current = 0;
    public int line = 1;

    static {
        keywords = new HashMap<>();
        keywords.put("BEGIN CODE", TokenType.BEGIN);
        keywords.put("END CODE", TokenType.END);
        keywords.put("ELSE", TokenType.ELSE);
        keywords.put("IF", TokenType.IF);
        keywords.put("DISPLAY", TokenType.DISPLAY);
        keywords.put("SCAN", TokenType.SCAN);
        keywords.put("TRUE", TokenType.TRUE);
        keywords.put("FALSE", TokenType.FALSE);
        keywords.put("WHILE", TokenType.WHILE);
        keywords.put("INT", TokenType.INT);
        keywords.put("CHAR", TokenType.CHAR);
        keywords.put("BOOL", TokenType.BOOL);
        keywords.put("FLOAT", TokenType.FLOAT);
    }
    

    
    Lexer(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
        
        start = current;
        scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }
        
    private void addToken(TokenType type) {
        addToken(type, null);
    }
        
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
        if (peek() == '\n') line++;
        advance();
        }
        if (isAtEnd()) {
        Lox.error(line, "Unterminated string.");
        return;
        }
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
        (c >= 'A' && c <= 'Z') ||
        c == '_';
    }

    private boolean isAlphaNumeric(char c) {
       return isAlpha(c) || isDigit(c);
    }
       
       
    private void scanToken() {
        char c = advance();
        switch (c) {
        case '(': addToken(TokenType.LEFT_PAREN); break;
        case ')': addToken(TokenType.RIGHT_PAREN); break;
        case '[': addToken(TokenType.ESC1); break;
        case ']': addToken(TokenType.ESC2); break;
        case '{': addToken(TokenType.LEFT_BRACE); break;
        case '}': addToken(TokenType.RIGHT_BRACE); break;
        case ',': addToken(TokenType.COMMA); break;
        case '.': addToken(TokenType.DOT); break;
        case '-': if (isDigit(peek())) {
                        number();
                    } else {
                        addToken(TokenType.MINUS);
                    }
        break;
        case '%': addToken(TokenType.MOD); break;
        case '+': addToken(TokenType.PLUS); break;
        case ':': addToken(TokenType.COLON); break;
        case '*': addToken(TokenType.STAR); break;
        case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
        case '<':
            if (match('>')) {
                addToken(TokenType.BANG_EQUAL);
            } else {
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            }
            break;
        case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
        case '/': addToken(TokenType.SLASH); break;
        case '$': addToken(TokenType.NLINE); line++; break;
        case '&': addToken(TokenType.CONCAT); break;
        case '#': while (peek() != '\n' && !isAtEnd()) {
                    advance();
                }
            break;
        case '\'':
            if (peek() == '\'') {
                Lox.error(line, "Empty character literal.");
            } else {
                char charValue = advance();
                if (match('\'')) {
                    addToken(TokenType.CHAR, charValue);
                } else {
                    Lox.error(line, "Unterminated character literal.");
                }
            }
            break;
        case ' ':
        case '\r':
        case '\t':
                  break;
        case '\n': line++; break;
        case '"': string(); break;
        case 'B':
            if (checkKeyword("EGIN")) {
                if (match(' ')) {
                    if (checkKeyword("IF")) {
                        addToken(TokenType.BEGIN_IF);
                    } else if (checkKeyword("WHILE")) {
                        addToken(TokenType.BEGIN_WHILE);
                    } else {
                        identifier();
                    }
                } else {
                    identifier();
                }
            } else {
                identifier();
            }
            break;
        case 'E':
            if (checkKeyword("ND")) {
                if (match(' ')) {
                    if (checkKeyword("IF")) {
                        addToken(TokenType.END_IF);
                    } else if (checkKeyword("WHILE")) {
                        addToken(TokenType.END_WHILE);
                    } else {
                        identifier();
                    }
                } else {
                    identifier();
                }
            } else {
                identifier();
            }
            break;
        default:
            if (isDigit(c)) {
                number();
            } else if (isAlpha(c)) {
                identifier();
            } else {
                Lox.error(line, "Unexpected character.");
            }
        }
    }

    private boolean checkKeyword(String expected) {
        if (current + expected.length() > source.length()) {
            return false;
        }
    
        for (int i = 0; i < expected.length(); i++) {
            if (source.charAt(current + i) != expected.charAt(i)) {
                return false;
            }
        }

        if (current + expected.length() < source.length() && isAlphaNumeric(source.charAt(current + expected.length()))) {
            return false;
        }
    
        current += expected.length();
        return true;
    }

}


