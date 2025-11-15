package com.dractical.femutils.paper.command;

import com.dractical.femutils.core.result.Result;

/**
 * Functional interface for command executors that return a {@link Result}.
 */
@FunctionalInterface
public interface CommandAction {

    /**
     * Executes the command.
     */
    Result<Void> execute(CommandContext context);
}
