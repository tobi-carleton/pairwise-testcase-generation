package org.tobi;

public class IndexedValue {
    private final int index;
    private final String parameter;
    private final String value;

    public IndexedValue(int index,String parameter, String value) {
        this.index = index;
        this.parameter = parameter;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public String getParameter() {
        return parameter;
    }

    public String getValue() {
        return value;
    }
}
