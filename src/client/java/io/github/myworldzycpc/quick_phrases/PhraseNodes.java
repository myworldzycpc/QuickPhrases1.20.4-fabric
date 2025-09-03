package io.github.myworldzycpc.quick_phrases;

import net.minecraft.client.gui.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PhraseNodes {
    public HashMap<String, Object> nodes;

    public PhraseNodes(HashMap<String, Object> nodes) {
        this.nodes = nodes;
    }

    public PhraseNodes() {
        nodes = new HashMap<>();
    }

    public Set<String> getKeys() {
        return nodes.keySet();
    }

    public boolean containsKey(String key) {
        return nodes.containsKey(key);
    }

    public boolean containsKey(List<String> path) {
        return get(path) != null;
    }

    public Object get(String key) {
        return nodes.get(key);
    }

    public Object get(List<String> path) {
        if (path.isEmpty()) {
            return this;
        }
        if (path.size() == 1) {
            return nodes.get(path.get(0));
        }
        PhraseNodes current = this;
        for (String key : path.subList(0, path.size() - 1)) {
            if (current.containsKey(key)) {
                Object value = current.get(key);
                if (value instanceof PhraseNodes) {
                    current = (PhraseNodes) value;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return current.get(path.get(path.size() - 1));
    }

    public void put(String key, Leaf value) {
        nodes.put(key, value);
    }

    public void put(String key, String value) {
        nodes.put(key, new Leaf(value));
    }

    public void put(String key, PhraseNodes value) {
        nodes.put(key, value);
    }

    public void put(List<String> path, Leaf value) {
        Object current = this.get(path.subList(0, path.size() - 1));
        if (current instanceof PhraseNodes) {
            ((PhraseNodes) current).put(path.get(path.size() - 1), value);
        } else {
            throw new IllegalArgumentException("Cannot put non-PhraseNodes value");
        }
    }

    public void put(List<String> path, String value) {
        put(path, new Leaf(value));
    }

    public void put(List<String> path, PhraseNodes value) {
        Object current = this.get(path.subList(0, path.size() - 1));
        if (current instanceof PhraseNodes) {
            ((PhraseNodes) current).put(path.get(path.size() - 1), value);
        } else {
            throw new IllegalArgumentException("Cannot put non-PhraseNodes value");
        }
    }

    public void remove(String key) {
        nodes.remove(key);
    }

    public void remove(List<String> path) {
        Object current = this.get(path.subList(0, path.size() - 1));
        if (current instanceof PhraseNodes) {
            ((PhraseNodes) current).remove(path.get(path.size() - 1));
        } else {
            throw new IllegalArgumentException("Cannot remove non-PhraseNodes value");
        }
    }

    public boolean isLeaf(String key) {
        if (nodes.containsKey(key)) {
            Object value = nodes.get(key);
            return value instanceof Leaf;
        }
        return false;
    }

    public void insert(List<String> path, String key, Leaf value) {
        PhraseNodes current = this;
        for (int i = 0; i < path.size() - 1; i++) {
            String currentKey = path.get(i);
            if (current.containsKey(currentKey)) {
                Object currentValue = current.get(currentKey);
                if (currentValue instanceof PhraseNodes) {
                    current = (PhraseNodes) currentValue;
                } else {
                    throw new IllegalArgumentException("Cannot insert non-PhraseNodes value");
                }
            } else {
                throw new IllegalArgumentException("Cannot insert non-existent key");
            }
        }
        current.put(key, value);
    }

    public boolean changeKey(List<String> oldPath, List<String> newPath) {
        PhraseNodes current = this;
        for (int i = 0; i < oldPath.size() - 1; i++) {
            String currentKey = oldPath.get(i);
            if (current.containsKey(currentKey)) {
                Object currentValue = current.get(currentKey);
                if (currentValue instanceof PhraseNodes) {
                    current = (PhraseNodes) currentValue;
                } else {
                    throw new IllegalArgumentException("Cannot change key of non-PhraseNodes value");
                }
            } else {
                throw new IllegalArgumentException("Cannot change key of non-existent key");
            }
        }
        Object value = current.get(oldPath.get(oldPath.size() - 1));
        if (current.containsKey(newPath.get(newPath.size() - 1))) {
            return false;
        }
        current.remove(oldPath.get(oldPath.size() - 1));
        if (value instanceof Leaf) {
            current.put(newPath.get(newPath.size() - 1), (Leaf) value);
        } else if (value instanceof PhraseNodes) {
            current.put(newPath.get(newPath.size() - 1), (PhraseNodes) value);
        } else {
            throw new IllegalArgumentException("Cannot change key of non-Leaf or non-PhraseNodes value");
        }
        return true;
    }

    public boolean changeKey(List<String> oldPath, String newKey) {
        List<String> newPath = new ArrayList<>(oldPath);
        newPath.set(newPath.size() - 1, newKey);
        return changeKey(oldPath, newPath);
    }
}
