package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ArrayEval;
import org.apache.poi.ss.formula.eval.IArrayEval;
import org.apache.poi.ss.formula.eval.ValueEval;

import java.util.Set;

/**
 * Created by serzh on 10/23/15.
 */
public abstract class BaseFreeRefFunction implements FreeRefFunction {
    public ValueEval evaluateArray(ValueEval[] args, OperationEvaluationContext ec) {
        int length = ArrayFunctionsHelper.getIArrayArg(args).getLength();
        IArrayEval[] arargs = new IArrayEval[args.length];
        for (int i = 0; i < args.length; i++) arargs[i] = ArrayFunctionsHelper.coerceToIArrayEval(args[i], length);
        int firstRow = ArrayFunctionsHelper.getFirstRow(args);
        int lastRow = ArrayFunctionsHelper.getLastRow(args, length - 1);


        ValueEval[] result = new ValueEval[length];
        for (int i = 0; i < length; i++) {
            ValueEval[] newArgs = new ValueEval[args.length];
            for (int j = 0; j < args.length; j++) newArgs[j] = arargs[j].getValue(i);

            // freeze args
            if (notArrayArgs() != null) {
                for (Integer j : notArrayArgs())
                    if (j < args.length)
                        newArgs[j] = args[j];
            }

            result[i] = evaluate(newArgs, ec);
        }
        return new ArrayEval(result, firstRow, lastRow);
    }

    public Set<Integer> notArrayArgs() {
        return null;
    }
}
