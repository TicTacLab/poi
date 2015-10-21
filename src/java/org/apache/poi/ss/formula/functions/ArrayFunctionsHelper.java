package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.LazyAreaEval;
import org.apache.poi.ss.formula.eval.ArrayEval;
import org.apache.poi.ss.formula.eval.IArrayEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;

import java.util.Set;

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
            if (isIArrayEval(valueEval)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAnyIArrayEval(ValueEval[] valueEvals, Set<Integer> excludes) {
        for (int i = 0; i < valueEvals.length; i++) {
            if (excludes != null && excludes.contains(i)) continue;
            ValueEval valueEval = valueEvals[i];
            if (isIArrayEval(valueEval)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIArrayEval(ValueEval ve) {
        return ve instanceof IArrayEval;
    }

    public static boolean isAnyArrayPtg(Ptg[] ptgs) {
        for (Ptg ptg : ptgs) {
            if (ptg instanceof AreaPtg) {
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

    public static int getFirstRow(ValueEval[] valueEvals) {
        for (ValueEval valueEval : valueEvals) {
            if (valueEval instanceof LazyAreaEval)
                return ((LazyAreaEval) valueEval).getFirstRow();
        }
        return 0;
    }

    public static int getLastRow(ValueEval[] valueEvals, int notFound) {
        for (ValueEval valueEval : valueEvals) {
            if (valueEval instanceof LazyAreaEval)
                return ((LazyAreaEval) valueEval).getLastRow();
        }
        return notFound;
    }
}
