package org.apache.poi.ss.formula.eval;

/**
 * Created by serzh on 10/14/15.
 */
public class ArrayEval implements ValueEval, IArrayEval {
    private ValueEval[] _values;
    int _firstRow;
    int _lastRow;

    public ArrayEval(ValueEval[] values, int firstRow, int lastRow) {
        this._values = values;
        this._firstRow = firstRow;
        this._lastRow = lastRow;
    }

    public ValueEval getValue(int i) {
        return _values[i];
    }

    public ValueEval getOffsetValue(int i) {
        return _values[i - _firstRow];
    }

    public ValueEval[] getValues() {
        return _values;
    }

    public int getLength() {
        return _values.length;
    }

    public int getColsNum() { return 1;  }

    @Override
    public int getFirstRow() {
        return _firstRow;
    }

    public String toString() {
        return this.getClass().getCanonicalName()
                + " [len=" + _values.length + "; " + _values[0]
                + ", ..., "
                + _values[_values.length-1]
                + "]";
    }
}
