package javarepl.examples;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import javarepl.console.Console;
import javarepl.console.ConsoleResult;
import javarepl.console.DelegatingConsole;
import javarepl.console.SimpleConsole;
import javarepl.console.rest.RestConsole;

import static com.googlecode.totallylazy.Sequences.one;
import static javarepl.console.ConsoleConfig.consoleConfig;
import static javarepl.console.ConsoleLog.error;
import static javarepl.console.ConsoleLog.success;
import static javarepl.console.commands.Command.parseStringCommand;

/**
 * Example shows how to write console wrapper that restricts JavaREPL access until user logs in with correct password.
 * More sophisticated (e.g. db or oauth backed) services can be created if needed.
 */
public class ConsoleWithAuthentication {

    public static void main(String... args) throws Exception {
        new RestConsole(new AuthConsole(new SimpleConsole(consoleConfig()), "password"), 8001)
                .start();
    }

    public static class AuthConsole extends DelegatingConsole {
        private final String password;
        private boolean authenticated = false;

        protected AuthConsole(Console delegate, String password) {
            super(delegate);
            this.password = password;
        }

        @Override
        public ConsoleResult execute(String expression) {
            if (!authenticated) {
                if (expression.startsWith(":login")) {
                    Pair<String, Option<String>> command = parseStringCommand(expression);
                    if (!command.second().isEmpty()) {
                        if (command.second().get().equals(password)) {
                            authenticated = true;
                            return new ConsoleResult(expression, one(success("Logged in")));
                        } else {
                            return new ConsoleResult(expression, one(error("Invalid password")));
                        }
                    } else {
                        return new ConsoleResult(expression, one(error("Please specify password")));
                    }
                } else {
                    return new ConsoleResult(expression, one(error("Please authenticate first.\n    :login <password> to authenticate.\n    :logout at the end of the session to finish.")));
                }
            } else {
                if (expression.startsWith(":logout")) {
                    authenticated = false;
                    return new ConsoleResult(expression, one(error("Logged out")));
                } else {
                    return super.execute(expression);
                }
            }
        }
    }
}
