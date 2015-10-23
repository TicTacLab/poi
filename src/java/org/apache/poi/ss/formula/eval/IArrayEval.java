package org.apache.poi.ss.formula.eval;

/**
 * Created by serzh on 10/15/15.
 */
public interface IArrayEval {
    ValueEval getValue(int x);
    ValueEval getOffsetValue(int row);
    ValueEval[] getValues();
    int getLength();
    int getColsNum();
    int getFirstRow();
}
