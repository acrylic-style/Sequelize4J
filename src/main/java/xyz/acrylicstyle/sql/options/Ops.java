package xyz.acrylicstyle.sql.options;

import org.jetbrains.annotations.NotNull;

public enum Ops {
    EQUAL("="),
    NOT_EQUAL("!="),
    GREATER_THAN(">"),
    GREATER_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_OR_EQUAL("<="),
    ;

    public final String op;

    Ops(@NotNull String op) {
        this.op = op;
    }
}
