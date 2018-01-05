package javarepl.console.commands;

import javarepl.Evaluator;
import javarepl.completion.CommandCompleter;
import javarepl.console.ConsoleLogger;

import static com.googlecode.totallylazy.predicates.Predicates.equalTo;

public final class QuitApplication extends Command {
    private static final String COMMAND = ":quit";
    private final Evaluator evaluator;
    private final ConsoleLogger logger;

    public QuitApplication(Evaluator evaluator, ConsoleLogger logger) {
        super(COMMAND + " - quit application", equalTo(COMMAND).or(equalTo(null)), new CommandCompleter(COMMAND));
        this.evaluator = evaluator;
        this.logger = logger;
    }

    public void execute(String expression) {
        logger.success("Terminating...");
        evaluator.clearOutputDirectory();
        System.exit(0);
    }
}
