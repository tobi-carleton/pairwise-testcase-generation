package org.tobi;

import java.util.List;

public class Parameter {
    private final String name;
    private final List<String> values;

    public Parameter(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }
}
