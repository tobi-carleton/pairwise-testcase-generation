package org.tobi;

public class IndexedValue {
    private final int index;
    private final int parameterIndex;
    private final String parameter;
    private final String value;

    public IndexedValue(int index, int parameterIndex, String parameter, String value) {
        this.index = index;
        this.parameterIndex = parameterIndex;
        this.parameter = parameter;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public String getParameter() {
        return parameter;
    }

    public String getValue() {
        return value;
    }
}
