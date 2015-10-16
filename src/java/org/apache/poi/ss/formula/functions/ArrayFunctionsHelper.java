package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.ArrayEval;
import org.apache.poi.ss.formula.eval.IArrayEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Created by serzh on 10/16/15.
 */
public class ArrayFunctionsHelper {

    public static IArrayEval coerceToIArrayEval(ValueEval ve, int length) {
        if (ve instanceof IArrayEval) {
            return (IArrayEval) ve;
        }

        ValueEval[] result = new ValueEval[length];
        for (int i = 0; i < length; i++) {
            result[i] = ve;
        }
        return new ArrayEval(result, 0, 1);
    }

    public static IArrayEval coerceToIArrayEval(ValueEval ve) {
        return (IArrayEval) ve;
    }

    public static boolean isAnyIArrayEval(ValueEval[] valueEvals) {
        for (ValueEval valueEval : valueEvals) {
            if (valueEval instanceof IArrayEval) {
                return true;
            }
        }
        return false;
    }

    public static IArrayEval getIArrayArg(ValueEval[] valueEvals) {
        for (ValueEval valueEval : valueEvals) {
            if (valueEval instanceof IArrayEval)
                return (IArrayEval) valueEval;
        }
        return null;
    }
}
