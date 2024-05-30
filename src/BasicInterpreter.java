import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class BasicInterpreter {
    public static void main(String[] args) {
        String filename = "C:\\int3rpr3t3r\\src\\Input.txt";
        String input = readFile(filename);

        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.scanTokens();
        
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        
        Semantic interpreter = new Semantic();
        interpreter.interpret(statements);
    }

    private static String readFile(String filename) {
        StringBuilder content = new StringBuilder();
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
            System.exit(1);
        }
        return content.toString();
    }
}
