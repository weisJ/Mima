import edu.kit.mima.script.parser.ScriptParser;
import edu.kit.mima.script.parser.ScriptTokenStream;
import edu.kit.mima.script.translator.ScriptTranslator;

/**
 * @author Jannis Weis
 * @since 2019
 */
public final class ParserTest {

    public static void main(final String[] args) {
//        var script = "v = 5;\n"
//                     + "if (v == 6) {\n"
//                     + "    v = v + 3;\n"
//                     + "}\n"
//                     + "if (!true) then {\n"
//                     + "        print(a);\n"
//                     + "} else {\n"
//                     + "    v = v / 2 * 3 % 1;\n"
//                     + "}";
//        var parse = new ScriptParser(new ScriptTokenStream(script)).parseTopLevel();
//        System.out.println(parse.toString());
//        System.out.println(new ScriptTranslator().translate(script));

        var script2 = "fun fun1(a,b,c) {\n"
                      + "v = a + b + c;\n"
                      + "return;"
                      + "}";
        var parse = new ScriptParser(new ScriptTokenStream(script2)).parseTopLevel();
        System.out.println(parse.toString());
        System.out.println(new ScriptTranslator().translate(script2));
    }
}
