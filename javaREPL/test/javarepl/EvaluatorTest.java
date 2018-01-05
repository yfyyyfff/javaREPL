package javarepl;

import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Option;
import javarepl.expressions.*;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.googlecode.totallylazy.Option.none;
import static javarepl.EvaluationClassLoader.evaluationClassLoader;
import static javarepl.EvaluationContext.evaluationContext;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EvaluatorTest {
    @Test
    public void shouldCreateExpressionOfCorrectType() {
        assertThat(evaluating("1 + 2"), hasExpressionOfType(Value.class));
        assertThat(evaluating("{}"), hasExpressionOfType(Statement.class));
        assertThat(evaluating("someVar = 12"), hasExpressionOfType(Assignment.class));
        assertThat(evaluating("int someVar = 12"), hasExpressionOfType(AssignmentWithType.class));
        assertThat(evaluating("import java.util.*"), hasExpressionOfType(Import.class));
        assertThat(evaluating("class SomeClass{}"), hasExpressionOfType(Type.class));
        assertThat(evaluating("interface SomeInterface{}"), hasExpressionOfType(Type.class));
        assertThat(evaluating("int max(int a, int b) { return a > b ? a : b; }"), hasExpressionOfType(Method.class));
    }

    @Test
    public void shouldCopyImportsOnNewClasses() {
        assertThat(evaluating("new NewClass{URL url;}"), hasCompilationError());
        assertThat(evaluating("import java.net.*", "class NewClass{URL url;}"), hasExpressionOfType(Type.class));
    }

    @Test
    public void shouldEvaluateExpressions() {
        assertThat(evaluating("\"hello \" + \n \"there\""), hasResult("hello there"));
        assertThat(evaluating("1 + 2"), hasResult(3));
        assertThat(evaluating("new String(\"hello there\")"), hasResult("hello there"));
        assertThat(evaluating("class NewClass {public int field=20;}"), hasNoResult());
        assertThat(evaluating("class NewClass {public int field=20;}", "new NewClass().field"), hasResult(20));
        assertThat(evaluating("int max(int a, int b) { return a > b ? a : b; }", "max(20, 30)"), hasResult(30));
        assertThat(evaluating("String res = null", "res"), hasResult(null));
        assertThat(evaluating("String a = \"My String\";", "String b = \"My String\";", "a == b"), hasResult(true));
    }

    @Test
    public void shouldShadowPreviousResult() {
        assertThat(evaluating("test = 21", "test = 42", "test"), hasResult(42));
        assertThat(evaluating("test = 21", "test++", "test"), hasResult(22));
    }

    @Test
    public void supportsRedefiningAndOverloadingMethods() {
        assertThat(evaluating("int method(){return 21;}", "int method(){return 42;}", "method()"), hasResult(42));
        assertThat(evaluating("int method(){return 21;}", "int method(int i){return i;}", "method() + method(21)"), hasResult(42));
    }

    @Test
    public void supportsMultipleOrNoSemiColonAtTheEndOfExpression() {
        assertThat(evaluating("int test = 42"), hasResult(42));
        assertThat(evaluating("int test = 42;;;"), hasResult(42));
    }

    @Test
    public void shouldReturnTypeOfExpression() {
        assertThat(evaluator().typeOfExpression("\"hello\""), is(Option.<java.lang.reflect.Type>some(String.class)));
        assertThat(evaluator().typeOfExpression("12"), is(Option.<java.lang.reflect.Type>some(Integer.class)));
        assertThat(evaluator().typeOfExpression("System.out.println(\"hello\")"), is(none(java.lang.reflect.Type.class)));
    }

    @Test
    public void shouldReturnClassFromExpression() {
        assertThat(evaluator().classFrom("String"), is(Option.<Class<?>>some(String.class)));
        assertThat(evaluator().classFrom("java.util.Map.Entry"), is(Option.<Class<?>>some(java.util.Map.Entry.class)));
        assertThat(evaluator().classFrom("invalid"), is(none(Class.class)));
    }

    private static Evaluator evaluator() {
        EvaluationContext context = evaluationContext();
        return new Evaluator(context, evaluationClassLoader(context));
    }

    private static Matcher<Either<? extends Throwable, Evaluation>> hasNoResult() {
        return new FeatureMatcher<Either<? extends Throwable, Evaluation>, Object>(Matchers.<Object>is(Result.noResult()), "result value", "result value") {
            protected Object featureValueOf(Either<? extends Throwable, Evaluation> evaluation) {
                return evaluation.right().result();
            }
        };
    }

    private static Matcher<Either<? extends Throwable, Evaluation>> hasCompilationError() {
        return new FeatureMatcher<Either<? extends Throwable, Evaluation>, Object>(Matchers.<Object>is(instanceOf(ExpressionCompilationException.class)), "result value", "result value") {
            protected Throwable featureValueOf(Either<? extends Throwable, Evaluation> evaluation) {
                return evaluation.left();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> Matcher<Either<? extends Throwable, Evaluation>> hasResult(T value) {
        return new FeatureMatcher<Either<? extends Throwable, Evaluation>, T>(is(value), "result value", "result value") {
            protected T featureValueOf(Either<? extends Throwable, Evaluation> evaluation) {
                return (T) evaluation.right().result().get().value();
            }
        };
    }


    private static Matcher<Either<? extends Throwable, Evaluation>> hasExpressionOfType(Class<?> clazz) {
        return new FeatureMatcher<Either<? extends Throwable, Evaluation>, Expression>(instanceOf(clazz), "result value", "result value") {
            protected Expression featureValueOf(Either<? extends Throwable, Evaluation> evaluation) {
                if (evaluation.isRight()) {
                    return evaluation.right().expression();
                } else {
                    throw new RuntimeException(evaluation.left());
                }
            }
        };
    }

    private static Either<? extends Throwable, Evaluation> evaluating(String... expressions) {
        Evaluator evaluator = evaluator();

        Either<? extends Throwable, Evaluation> result = null;
        for (String expression : expressions) {
            result = evaluator.evaluate(expression);
        }

        return result;
    }

}
