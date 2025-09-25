/*
 * AUTHORS:
 *          Maximilian Schulten (mls384@case.edu)
 *          Tommaso Beretta     (txb341@case.edu)
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpartieScanner {
    private String source;

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("while", TokenType.WHILE);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("fun", TokenType.FUN);
        keywords.put("return", TokenType.RETURN);
        keywords.put("var", TokenType.VAR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("null", TokenType.NULL);
    }

    public SpartieScanner(String source) {
        this.source = source;
    }

    public List<Token> scan() {
        List<Token> tokens = new ArrayList<>();

        Token token = null;
        while (!isAtEnd() && (token = getNextToken()) != null) {
            if (token.type != TokenType.IGNORE) tokens.add(token);
        }

        return tokens;
    }

    private Token getNextToken() {
        Token token = null;

        // Try to get each type of token, starting with a simple token, and getting a little more complex
        token = getSingleCharacterToken();
        if (token == null) token = getComparisonToken();
        if (token == null) token = getDivideOrComment();
        if (token == null) token = getStringToken();
        if (token == null) token = getNumericToken();
        if (token == null) token = getIdentifierOrReservedWord();
        if (token == null) {
            error(line, String.format("Unexpected character '%c' at %d", source.charAt(current), current));
        }

        return token;
    }

    private Token getSingleCharacterToken() {
        // Hint: Examine the character, if you can get a token, return it, otherwise return null
        // Hint: Be careful with the divide, we have ot know if it is a single character

        char nextCharacter = source.charAt(current);

        StringBuilder tokenString = new StringBuilder(String.valueOf(nextCharacter));

        // Hint: Start of not knowing what the token is, if we can determine it, return it, otherwise, return null
        TokenType type = TokenType.UNDEFINED;

        switch (nextCharacter) {
            case ' ': // Spaces are ignored 
                type = TokenType.IGNORE;
                break;
            case '\n': // Line break
                type = TokenType.IGNORE;
                line++; // Increment line counter
                break;
            case '+': // Addition
                type = TokenType.ADD;
                break;
            case '-': // Subtraction
                type = TokenType.SUBTRACT;
                break;
            case '*': // Multiplication
                type = TokenType.MULTIPLY;
                break;
            case ';': // Semicolon
                type = TokenType.SEMICOLON;
                break;
            case '{': // Left Brace
                type = TokenType.LEFT_BRACE;
                break;
            case '}': // Right Brace
                type = TokenType.RIGHT_BRACE;
                break;
            case '(': // Left Parentheses
                type = TokenType.LEFT_PAREN;
                break;
            case ')': // Right Parentheses
                type = TokenType.RIGHT_PAREN;
                break;
            case ',': // Comma
                type = TokenType.COMMA;
                break;
            case '=': // Assignment
                if(examine_safe('=')) {return null;}
                type = TokenType.ASSIGN;
                break;
            case '!': // Negation
                if(examine_safe('=')) {return null;}
                type = TokenType.NOT;
                break;
            default:
                return null;
        }

        if(type == TokenType.UNDEFINED) return null;
        
        current = current + (tokenString.toString().length());

        return new Token(type, String.valueOf(nextCharacter), this.line); 
    }

    // TODO: Complete implementation
    private Token getComparisonToken() {
        // Hint: Examine the character for a comparison but check the next character (as long as one is available)
        // For example: < or <=
        
        char nextCharacter = source.charAt(current);

        TokenType type = TokenType.UNDEFINED;

        StringBuilder tokenString = new StringBuilder(String.valueOf(nextCharacter));

        switch (nextCharacter) {
            case '|': // Check easy tokens first
                type = TokenType.OR;
                break;
            case '&':
                type = TokenType.AND;
                break;
            case '<': // Check if equals there
                if(examine_safe('=')) {
                    type = TokenType.LESS_EQUAL;
                    tokenString.append('=');
                } else {
                    type = TokenType.LESS_THAN;
                }
                break;
            case '>': // Check if equals there
                if(examine_safe('=')) {
                    type = TokenType.GREATER_EQUAL;
                    tokenString.append('=');
                } else {
                    type = TokenType.GREATER_THAN;
                }
                break;
            case '!': // Do not need to check for equals character since negation is picked up in getSingleCharacterToken()
                if(examine_safe('=')) {
                    type = TokenType.NOT_EQUAL;
                    tokenString.append('=');
                }
                break;
            case '=': // Do not need to check for assignments is picked up in getSingleCharacterToken()
                if(examine_safe('=')) {
                    type = TokenType.EQUIVALENT;
                    tokenString.append('=');
                } 
                break;
            default:
                return null;
        }

        if (type == TokenType.UNDEFINED) return null;

        current = current + (tokenString.toString().length());

        return new Token(type, tokenString.toString(), line);
    }

    // TODO: Complete implementation
    private Token getDivideOrComment() {
        // Hint: Examine the character for a comparison but check the next character (as long as one is available)
        char nextCharacter = source.charAt(current);

        TokenType type = TokenType.UNDEFINED;

        StringBuilder tokenString = new StringBuilder(String.valueOf(nextCharacter));

        switch (nextCharacter) {
            case '/': // Check if equals there
                if(examine_safe('/')) {
                    type = TokenType.IGNORE;
                    int i = current+1;
                    while((i < source.length()) && (source.charAt(i) != '\n')) {
                        tokenString.append(source.charAt(i));
                        i++;
                    }
                } else {
                    type = TokenType.DIVIDE;
                }
                break;
            default:
                return null;
        }

        if (type == TokenType.UNDEFINED) return null;

        current = current + (tokenString.toString().length());

        return new Token(type, tokenString.toString(), line);
    }

    // TODO: Complete implementation
    private Token getStringToken() {
        // Hint: Check if you have a double quote, then keep reading until you hit another double quote
        // But, if you do not hit another double quote, you should report an error
        char nextCharacter = source.charAt(current);

        TokenType type = TokenType.UNDEFINED;

        StringBuilder tokenString = new StringBuilder();

        switch (nextCharacter) {
            case '"': // Check if equals there
                type = TokenType.STRING;
                boolean stringTerminated = false;
                int i = current+1; // Enter the quotes
                while((i < source.length())) {
                    if (source.charAt(i) == '"') {
                        stringTerminated = true;
                        break;
                    }
                    tokenString.append(source.charAt(i)); 
                    i++;
                }
                if(!stringTerminated) {
                    error(line, "Expected a closing: '" + '"' + "', but none found.");
                }
                break;
            default:
                return null;
        }

        if (type == TokenType.UNDEFINED) return null;

        current = current + (tokenString.toString().length()) + 2; // +2 for quotes

        return new Token(type, tokenString.toString(), line);
    }

    // TODO: Complete implementation
    private Token getNumericToken() {
        // Hint: Follow similar idea of String, but in this case if it is a digit
        // You should only allow one period in your scanner
        char nextCharacter = source.charAt(current);

        TokenType type = TokenType.UNDEFINED;

        StringBuilder tokenString = new StringBuilder(String.valueOf(nextCharacter));

        if (isDigit(nextCharacter)) {
            type = TokenType.NUMBER;
            int i = current+1;
            boolean decimal = false;
            while(i < source.length()) {
                char character = source.charAt(i);
                if (isDigit(character)) {
                    tokenString.append(character);
                } else if (decimal && character == '.') {
                    break;
                } else if (character == '.') {
                    decimal = true;
                    tokenString.append(character);
                } else {
                    break;
                }
                i++;
            }
        }

        if (type == TokenType.UNDEFINED) return null;

        current = current + (tokenString.toString().length());

        return new Token(type, tokenString.toString(), line);
    }

    // TODO: Complete implementation
    private Token getIdentifierOrReservedWord() {
        // Hint: Assume first it is an identifier and once you capture it, then check if it is a reserved word.
        char nextCharacter = source.charAt(current);

        TokenType type = TokenType.UNDEFINED;

        StringBuilder tokenString = new StringBuilder(String.valueOf(nextCharacter));

        if (isAlpha(nextCharacter)) {
            int i = current+1;
            while((i < source.length()) && (isAlpha(source.charAt(i)))) {
                char character = source.charAt(i);
                tokenString.append(character);
                i++;
            }

            type = keywords.get(tokenString.toString()); // Check if captured token is a reserved word
            if(type == null) {
                type = TokenType.IDENTIFIER;
            }
        }

        if (type == TokenType.UNDEFINED) return null;

        current = current + (tokenString.toString().length());

        return new Token(type, tokenString.toString(), line);
    }
    
    // Helper Methods
    private boolean isDigit(char character) {
        return character >= '0' && character <= '9';
    }

    private boolean isAlpha(char character) {
        return character >= 'a' && character <= 'z' ||
                character >= 'A' && character <= 'Z';
    }

    // This will check if a character is what you expect, if so, it will advance
    // Useful for checking <= or //
    private boolean examine(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current + 1) != expected) return false;

        // Otherwise, it matches it, so advance
        return true;
    }

    private boolean examine_safe(char expected) {
        if(current + 1 < source.length()) {
            return examine(expected);
        } else return false;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Error handling
    private void error(int line, String message) {
        System.err.printf("Error occurred on line %d : %s\n", line, message);
        System.exit(ErrorCode.INTERPRET_ERROR);
    }
}
