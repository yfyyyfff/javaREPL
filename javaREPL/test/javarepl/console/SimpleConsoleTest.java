package javarepl.console;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.predicates.Predicates;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import static javarepl.console.ConsoleConfig.consoleConfig;
import static javarepl.console.ConsoleLog.Type.ERROR;
import static javarepl.console.ConsoleLog.Type.SUCCESS;
import static javarepl.console.ConsoleLog.error;
import static javarepl.console.ConsoleLog.success;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;


public class SimpleConsoleTest {
    @Test
    public void supportsPrintingToSystemStreams() {
        assertThat(executing("System.out.println(\"test\")"), hasLogged(success("test")));
        assertThat(executing("System.err.println(\"test\")"), hasLogged(error("test")));
    }

    @Test
    public void providesFeedbackAfterEvaluation() {
        assertThat(executing("import java.net.*;"), hasLogged(success("Imported java.net.*")));
        assertThat(executing("int method(Integer i){return i;}"), hasLogged(success("Created method int method(java.lang.Integer)")));
        assertThat(executing("class SomeClass{}"), hasLogged(success("Created type SomeClass")));
        assertThat(executing("int i = 42"), hasLogged(success("int i = 42")));
    }

    @Test
    public void supportsDisplayingSingleResult() {
        assertThat(
                executing("42", "\"test\"", "res1"),
                hasLogged(success("java.lang.String res1 = \"test\"")));
    }

    @Test
    public void supportsDisplayingTypeOfExpression() {
        assertThat(
                executing(":type new Date()"),
                hasLogged(success("java.util.Date")));
    }

    @Test
    public void supportsListingOfResults() {
        assertThat(
                executing("import java.io.*", "42", "\"test\"", ":list results"),
                hasLogged(success("Results:\n" +
                        "    java.lang.Integer res0 = 42\n" +
                        "    java.lang.String res1 = \"test\"\n")));
    }

    @Test
    public void supportsListingOfImports() {
        assertThat(
                executing("import java.io.*;", "\"test\"", "import java.net.*", ":list imports"),
                hasLogged(success("Imports:\n" +
                        "    java.lang.*\n" +
                        "    java.util.*\n" +
                        "    java.math.*\n" +
                        "    java.lang.Math.*\n" +
                        "    java.util.function.*\n"+
                        "    java.io.*\n" +
                        "    java.net.*\n")));
    }

    @Test
    public void supportsListingOfTypes() {
        assertThat(
                executing("class AClass{}", "\"test\"", "interface AnInterface{}", ":list types"),
                hasLogged(success("Types:\n" +
                        "    AClass\n" +
                        "    AnInterface\n")));
    }

    @Test
    public void supportsCheckingOfSyntax() {
        assertThat(executing(":check 12 + 22"), hasLogged(
                success("Parsing:  Value(12 + 22)"),
                success("Evaluation:  Value(12 + 22) => java.lang.Integer res0 = 34")));

        assertThat(executing(":check foo"), hasLogged(
                success("Parsing:  Value(foo)"),
                error("Evaluation:  javarepl.ExpressionCompilationException: ERROR: not a statement\n" +
                        "    foo;\n" +
                        "    ^\n" +
                        "\n" +
                        "ERROR: cannot find symbol\n" +
                        "  symbol:   variable foo\n" +
                        "  location: class Evaluation\n" +
                        "    foo;\n" +
                        "    ^")));
    }

    @Test
    public void supportsListingOfMethods() {
        assertThat(
                executing("void method1(){}", "\"test\"", "Integer method2(int p1, String p2){return p1;}", ":list methods"),
                hasLogged(success("Methods:\n" +
                        "    void method1()\n" +
                        "    java.lang.Integer method2(int, java.lang.String)\n")));
    }

    @Test
    public void supportsListingOfAllValues() {
        Matcher<ConsoleResult> hasListedAllValues = hasLogged(
                success("Results:\n" +
                        "    java.lang.String res0 = \"test\"\n"),
                success("Types:\n" +
                        "    AClass\n"),
                success("Imports:\n" +
                        "    java.lang.*\n" +
                        "    java.util.*\n" +
                        "    java.math.*\n" +
                        "    java.lang.Math.*\n" +
                        "    java.util.function.*\n"+
                        "    java.net.*\n"),
                success("Methods:\n" +
                        "    void method1()\n"));

        assertThat(executing("void method1(){}", "\"test\"", "import java.net.*", "class AClass{}", ":list"), hasListedAllValues);
        assertThat(executing("void method1(){}", "\"test\"", "import java.net.*", "class AClass{}", ":list unknown_type"), hasListedAllValues);
    }

    @Test
    public void supportsListingHistoryOfEvaluations() {
        assertThat(
                executing(":hist"),
                hasLogged(success("History:\n" +
                        "    1  :hist\n")));

        assertThat(
                executing("42", "\"test\"", ":hist"),
                hasLogged(success("History:\n" +
                        "    1  42\n" +
                        "    2  \"test\"\n" +
                        "    3  :hist\n")));

        assertThat(
                executing("42", "21", "\"test\"", ":hist 2"),
                hasLogged(success("History:\n" +
                        "    3  \"test\"\n" +
                        "    4  :hist 2\n")));
    }


    @Test
    public void supportsSearchingHistory() {
        assertThat(
                executing("42", "\"test 1\"", "\"test 2\"", ":h?"),
                hasLogged(success("History search for '':\n" +
                        "    1  42\n" +
                        "    2  \"test 1\"\n" +
                        "    3  \"test 2\"\n" +
                        "    4  :h?\n")));

        assertThat(
                executing("42", "\"test 1\"", "\"test 2\"", ":h? test"),
                hasLogged(success("History search for 'test':\n" +
                        "    2  \"test 1\"\n" +
                        "    3  \"test 2\"\n" +
                        "    4  :h? test\n")));
    }

    @Test
    public void supportsEvaluatingFromHistory() {
        assertThat(
                executing("42", "\"test\"", ":h!", ":hist"),
                hasLogged(success("History:\n" +
                        "    1  42\n" +
                        "    2  \"test\"\n" +
                        "    3  \"test\"\n" +
                        "    4  :hist\n")));

        assertThat(
                executing("42", "\"test\"", ":h! 1", ":hist"),
                hasLogged(success("History:\n" +
                        "    1  42\n" +
                        "    2  \"test\"\n" +
                        "    3  42\n" +
                        "    4  :hist\n")));
    }

    @Test
    public void supportsResettingAllEvaluations() {
        assertThat(
                executing("42", "\"test\"", ":reset", ":list results"),
                hasLogged(success("Results:\n    \n")));
    }

    @Test
    public void supportsEvaluatingFileWithExpressions() {
        String path = "test/resources/expressions.txt";

        assertThat(
                executing(":eval " + path),
                hasLogged(
                        success("Created method void method()"),
                        success("Hello world"),
                        success("int num = 42"),
                        success("Finished evaluating " + path)));
    }

    public static int reevaluationCounter;

    @Test
    public void supportsReevaluatingAllExpression() {
        reevaluationCounter = 0;
        assertThat(
                executing("++javarepl.console.SimpleConsoleTest.reevaluationCounter", ":replay", "res0"),
                hasLogged(success("java.lang.Integer res0 = 2")));
    }


    private static ConsoleResult executing(String... items) {
        ConsoleConfig config = consoleConfig();
        SimpleConsole console = new SimpleConsole(config);
        console.start();

        System.setOut(new ConsoleLoggerPrintStream(SUCCESS, Predicates.<String>never(), config.logger));
        System.setErr(new ConsoleLoggerPrintStream(ERROR, Predicates.<String>never(), config.logger));

        ConsoleResult result = null;
        for (String item : items) {
            result = console.execute(item);
        }

        return result;
    }

    private static Matcher<ConsoleResult> hasLogged(final ConsoleLog... logs) {
        return new FeatureMatcher<ConsoleResult, Sequence<ConsoleLog>>(hasItems(logs), "console log", "console log") {
            protected Sequence<ConsoleLog> featureValueOf(ConsoleResult consoleResult) {
                return consoleResult.logs();
            }
        };
    }
}
