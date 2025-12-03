package net.bichal.bplb.gui.config;

import net.bichal.bplb.gui.Config;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ConfigState {
    private final Deque<ConfigSnapshot> undoStack = new ArrayDeque<>(20);
    private final Deque<ConfigSnapshot> redoStack = new ArrayDeque<>(20);
    private ConfigSnapshot current;
    private boolean dirty = false;

    public ConfigState(Config config) {
        this.current = new ConfigSnapshot(config);
    }

    public void pushChange(Config config) {
        redoStack.clear();
        if (undoStack.size() >= 20) undoStack.removeFirst();
        undoStack.addLast(current);
        current = new ConfigSnapshot(config);
        dirty = true;
    }

    public boolean undo(Config config) {
        if (undoStack.isEmpty()) return false;
        redoStack.addLast(current);
        current = undoStack.removeLast();
        current.applyTo(config);
        return true;
    }

    public boolean redo(Config config) {
        if (redoStack.isEmpty()) return false;
        undoStack.addLast(current);
        current = redoStack.removeLast();
        current.applyTo(config);
        return true;
    }

    public boolean hasChangesFromInitial(Config config) {
        ConfigSnapshot currentSnapshot = new ConfigSnapshot(config);
        return !snapshotsEqual(currentSnapshot, undoStack.isEmpty() ? current : undoStack.getFirst());
    }

    private boolean snapshotsEqual(ConfigSnapshot s1, ConfigSnapshot s2) {
        return true;
    }

    public boolean isDirty() {return dirty;}
    public void markClean() {dirty = false;}
    public boolean canUndo() {return !undoStack.isEmpty();}
    public boolean canRedo() {return !redoStack.isEmpty();}
}