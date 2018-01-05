package javarepl.console.commands;

import javarepl.Evaluator;
import javarepl.completion.CommandCompleter;
import javarepl.console.ConsoleLogger;

import static com.googlecode.totallylazy.predicates.Predicates.equalTo;

public final class ShowLastSource extends Command {
    private static final String COMMAND = ":src";

    private final Evaluator evaluator;
    private final ConsoleLogger logger;

    public ShowLastSource(Evaluator evaluator, ConsoleLogger logger) {
        super(COMMAND + " - show source of last evaluated expression", equalTo(COMMAND), new CommandCompleter(COMMAND));
        this.evaluator = evaluator;
        this.logger = logger;
    }

    public void execute(String expression) {
        logger.success(evaluator.lastSource().getOrElse("No source"));
    }
}
