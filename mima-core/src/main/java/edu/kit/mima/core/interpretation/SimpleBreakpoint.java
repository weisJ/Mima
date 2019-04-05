package edu.kit.mima.core.interpretation;

/**
 * Simple breakpoint implementation.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class SimpleBreakpoint implements Breakpoint {
    private final int lineIndex;

    public SimpleBreakpoint(final int lineIndex) {
        this.lineIndex = lineIndex;
    }

    @Override
    public int getLineIndex() {
        return lineIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Breakpoint) {
            return ((Breakpoint) o).getLineIndex() == this.getLineIndex();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return lineIndex;
    }
}
