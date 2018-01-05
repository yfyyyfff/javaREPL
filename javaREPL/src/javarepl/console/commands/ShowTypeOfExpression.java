package javarepl.console.commands;

import com.googlecode.totallylazy.Option;
import javarepl.Evaluator;
import javarepl.completion.CommandCompleter;
import javarepl.console.ConsoleLogger;

import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Strings.startsWith;
import static javarepl.rendering.TypeRenderer.renderType;

public final class ShowTypeOfExpression extends Command {
    private static final String COMMAND = ":type";
    private final Evaluator evaluator;
    private final ConsoleLogger logger;

    public ShowTypeOfExpression(Evaluator evaluator, ConsoleLogger logger) {
        super(COMMAND + " <expression> - shows the type of an expression without affecting current context", startsWith(COMMAND), new CommandCompleter(COMMAND));

        this.evaluator = evaluator;
        this.logger = logger;
    }

    public void execute(String expression) {
        Option<Type> expressionType = evaluator.typeOfExpression(parseStringCommand(expression).second().getOrElse(""));

        if (!expressionType.isEmpty()) {
            logger.success(renderType(expressionType.get()));
        } else {
            logger.error("Cannot determine the type of this expression.");
        }
    }

}
