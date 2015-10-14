package org.apache.poi.ss.formula.eval;

/**
 * Created by serzh on 10/14/15.
 */
public class ArrayEval implements ValueEval {
    private ValueEval[] _values;

    public ArrayEval(ValueEval[] values) {
        this._values = values;
    }

    public ValueEval getValue(int i) {
        return _values[i];
    }

    public ValueEval[] getValues() {
        return _values;
    }

    public String toString() {
        return this.getClass().getCanonicalName()
                + " [" + _values[0]
                + ", ..., "
                + _values[_values.length-1]
                + "]";
    }
}
