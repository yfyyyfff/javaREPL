package javarepl.console.commands;

import javarepl.Evaluator;
import javarepl.completion.CommandCompleter;
import javarepl.console.ConsoleLogger;
import javarepl.expressions.Import;
import javarepl.expressions.Method;
import javarepl.expressions.Type;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.startsWith;
import static javarepl.Utils.listValues;

public final class ListValues extends Command {
    private static final String COMMAND = ":list";
    private final Evaluator evaluator;
    private final ConsoleLogger logger;


    public ListValues(Evaluator evaluator, ConsoleLogger logger) {
        super(COMMAND + " <results|types|methods|imports|all> - list specified values",
                startsWith(COMMAND), new CommandCompleter(COMMAND, sequence("results", "methods", "imports", "types", "all")));

        this.evaluator = evaluator;
        this.logger = logger;
    }

    public void execute(String expression) {
        String items = expression.replace(COMMAND, "").trim();

        if (items.equals("results")) {
            listResults();
        } else if (items.equals("types")) {
            listTypes();
        } else if (items.equals("imports")) {
            listImports();
        } else if (items.equals("methods")) {
            listMethods();
        } else {
            listResults();
            listTypes();
            listImports();
            listMethods();
        }
    }

    private void listMethods() {
        logger.success(listValues("Methods", sequence(evaluator.expressionsOfType(Method.class)).map(Method::signature)));
    }

    private void listImports() {
        logger.success(listValues("Imports", sequence(evaluator.expressionsOfType(Import.class)).map(Import::typePackage)));
    }

    private void listTypes() {
        logger.success(listValues("Types", sequence(evaluator.expressionsOfType(Type.class)).map(Type::type)));
    }

    private void listResults() {
        logger.success(listValues("Results", evaluator.results()));
    }
}
