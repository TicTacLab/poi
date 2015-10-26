/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.ThreeDEval;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.usermodel.FormulaError;

import java.util.regex.Pattern;

/**
 * Common logic for COUNT, COUNTA and COUNTIF
 *
 * @author Josh Micich
 */
final class CountUtils {

	private CountUtils() {
		// no instances of this class
	}

	/**
	 * When the second argument is a string, many things are possible
	 */
	private static I_MatchPredicate createGeneralMatchPredicate(StringEval stringEval) {
		String value = stringEval.getStringValue();
		CmpOp operator = CmpOp.getOperator(value);
		value = value.substring(operator.getLength());

		Boolean booleanVal = parseBoolean(value);
		if(booleanVal != null) {
			return new BooleanMatcher(booleanVal.booleanValue(), operator);
		}

		Double doubleVal = OperandResolver.parseDouble(value);
		if(doubleVal != null) {
			return new NumberMatcher(doubleVal.doubleValue(), operator);
		}
		ErrorEval ee = parseError(value);
		if (ee != null) {
			return new ErrorMatcher(ee.getErrorCode(), operator);
		}

		//else - just a plain string with no interpretation.
		return new StringMatcher(value, operator);
	}

	private static ErrorEval parseError(String value) {
		if (value.length() < 4 || value.charAt(0) != '#') {
			return null;
		}
		if (value.equals("#NULL!"))  return ErrorEval.NULL_INTERSECTION;
		if (value.equals("#DIV/0!")) return ErrorEval.DIV_ZERO;
		if (value.equals("#VALUE!")) return ErrorEval.VALUE_INVALID;
		if (value.equals("#REF!"))   return ErrorEval.REF_INVALID;
		if (value.equals("#NAME?"))  return ErrorEval.NAME_INVALID;
		if (value.equals("#NUM!"))   return ErrorEval.NUM_ERROR;
		if (value.equals("#N/A"))    return ErrorEval.NA;

		return null;
	}

	/**
	 * Boolean literals ('TRUE', 'FALSE') treated similarly but NOT same as numbers.
	 */
	/* package */ static Boolean parseBoolean(String strRep) {
		if (strRep.length() < 1) {
			return null;
		}
		switch(strRep.charAt(0)) {
			case 't':
			case 'T':
				if("TRUE".equalsIgnoreCase(strRep)) {
					return Boolean.TRUE;
				}
				break;
			case 'f':
			case 'F':
				if("FALSE".equalsIgnoreCase(strRep)) {
					return Boolean.FALSE;
				}
				break;
		}
		return null;
	}

	/**
	 * Common interface for the matching criteria.
	 */
	public interface I_MatchPredicate {
		boolean matches(ValueEval x);
	}
    public interface I_MatchAreaPredicate extends I_MatchPredicate {
        boolean matches(TwoDEval x, int rowIndex, int columnIndex);
    }

    public static ThreeDEval[] getThreeDEvals(Object[] args) {
        int length = args.length/2;
        ThreeDEval[] result = new ThreeDEval[length];
        for (int i = 0; i < length; i++)
            result[i] = (ThreeDEval) args[i*2];
        return result;
    }

    public static RefEval[] getRefEvals(Object[] args) {
        int length = args.length/2;
        RefEval[] result = new RefEval[length];
        for (int i = 0; i < length; i++)
            result[i] = (RefEval) args[i*2];
        return result;
    }

    public static ValueEval[] getPredicates(Object[] args) {
        int length = args.length/2;
        ValueEval[] result = new ValueEval[length];
        for (int i = 0; i < length; i++) {
            result[i] = (ValueEval) args[i*2+1];
        }
        return result;
    }

    /**
     * @return the number of evaluated cells in the range that match the specified criteria
     */
    public static int countMatchingCellsInArea(Object... args) {
        int result = 0;

        ThreeDEval[] ranges = getThreeDEvals(args);

        ThreeDEval areaEval = ranges[0];

        for (int sIx=areaEval.getFirstSheetIndex(); sIx <= areaEval.getLastSheetIndex(); sIx++) {
            int height = areaEval.getHeight();
            int width = areaEval.getWidth();
            for (int rrIx=0; rrIx<height; rrIx++) {
                for (int rcIx=0; rcIx<width; rcIx++) {

                    boolean m = true;

                    for (int i = 0; i < ranges.length; i++) {

                        I_MatchPredicate criteriaPredicate = createCriteriaPredicate(args[i*2+1], rrIx, rcIx);
                        ValueEval ve = ranges[i].getValue(sIx, rrIx, rcIx);
                        if(criteriaPredicate instanceof I_MatchAreaPredicate){
                            I_MatchAreaPredicate areaPredicate = (I_MatchAreaPredicate)criteriaPredicate;
                            if(!areaPredicate.matches(areaEval, rrIx, rcIx)) {
								m = false;
                                break;
                            }
                        }
                        if(!criteriaPredicate.matches(ve)) {
                            m = false;
                            break;
                        }
                    }

                    if (m) result++;
                }
            }
        }
        return result;
    }
	/**
     * @return the number of evaluated cells in the range that match the specified criteria
	 */
	public static int countMatchingCellsInRef(Object... args) {
	    int result = 0;

        RefEval[] ranges = getRefEvals(args);

        RefEval refEval = ranges[0];
	    
	    for (int sIx = refEval.getFirstSheetIndex(); sIx <= refEval.getLastSheetIndex(); sIx++) {
            boolean m = true;
            for (int i = 0; i < ranges.length; i++) {

                ValueEval ve = ranges[i].getInnerValueEval(sIx);

                if(!createCriteriaPredicate(args[i*2+1], 0, 0).matches(ve)) {
                    m = false;
                    break;
                }
            }
            if (m) result++;

	    }
		return result;
	}
	public static int countArg(ValueEval eval, I_MatchPredicate criteriaPredicate) {
		if (eval == null) {
			throw new IllegalArgumentException("eval must not be null");
		}
        if (eval instanceof ThreeDEval) {
            return countMatchingCellsInArea(eval, criteriaPredicate);
        }
		if (eval instanceof TwoDEval) {
		    throw new IllegalArgumentException("Count requires 3D Evals, 2D ones aren't supported");
		}
		if (eval instanceof RefEval) {
			return CountUtils.countMatchingCellsInRef(eval, criteriaPredicate);
		}
		return criteriaPredicate.matches(eval) ? 1 : 0;
	}


	private static ValueEval evaluateCriteriaArg(ValueEval arg, int srcRowIndex, int srcColumnIndex) {
		try {
			return OperandResolver.getSingleValue(arg, srcRowIndex, srcColumnIndex);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	static I_MatchPredicate createCriteriaPredicate(Object arg, int srcRowIndex, int srcColumnIndex) {

		if (arg instanceof  I_MatchPredicate)
			return (I_MatchPredicate) arg;
		else if (arg instanceof ValueEval) {
			ValueEval evaluatedCriteriaArg = evaluateCriteriaArg((ValueEval) arg, srcRowIndex, srcColumnIndex);

			if (evaluatedCriteriaArg instanceof NumberEval) {
				return new NumberMatcher(((NumberEval) evaluatedCriteriaArg).getNumberValue(), CmpOp.OP_NONE);
			}
			if (evaluatedCriteriaArg instanceof BoolEval) {
				return new BooleanMatcher(((BoolEval) evaluatedCriteriaArg).getBooleanValue(), CmpOp.OP_NONE);
			}

			if (evaluatedCriteriaArg instanceof StringEval) {
				return createGeneralMatchPredicate((StringEval) evaluatedCriteriaArg);
			}
			if (evaluatedCriteriaArg instanceof ErrorEval) {
				return new ErrorMatcher(((ErrorEval) evaluatedCriteriaArg).getErrorCode(), CmpOp.OP_NONE);
			}
			if (evaluatedCriteriaArg == BlankEval.instance) {
				return null;
			}
			throw new RuntimeException("Unexpected type for criteria ("
					+ evaluatedCriteriaArg.getClass().getName() + ")");
		} else {
			throw new RuntimeException("Unexpected type for criteria ("
					+ arg.getClass().getName() + ")");
		}
	}

	static final class CmpOp {
		public static final int NONE = 0;
		public static final int EQ = 1;
		public static final int NE = 2;
		public static final int LE = 3;
		public static final int LT = 4;
		public static final int GT = 5;
		public static final int GE = 6;

		public static final CmpOp OP_NONE = op("", NONE);
		public static final CmpOp OP_EQ = op("=", EQ);
		public static final CmpOp OP_NE = op("<>", NE);
		public static final CmpOp OP_LE = op("<=", LE);
		public static final CmpOp OP_LT = op("<", LT);
		public static final CmpOp OP_GT = op(">", GT);
		public static final CmpOp OP_GE = op(">=", GE);
		private final String _representation;
		private final int _code;

		private static CmpOp op(String rep, int code) {
			return new CmpOp(rep, code);
		}
		private CmpOp(String representation, int code) {
			_representation = representation;
			_code = code;
		}
		/**
		 * @return number of characters used to represent this operator
		 */
		public int getLength() {
			return _representation.length();
		}
		public int getCode() {
			return _code;
		}
		public static CmpOp getOperator(String value) {
			int len = value.length();
			if (len < 1) {
				return OP_NONE;
			}

			char firstChar = value.charAt(0);

			switch(firstChar) {
				case '=':
					return OP_EQ;
				case '>':
					if (len > 1) {
						switch(value.charAt(1)) {
							case '=':
								return OP_GE;
						}
					}
					return OP_GT;
				case '<':
					if (len > 1) {
						switch(value.charAt(1)) {
							case '=':
								return OP_LE;
							case '>':
								return OP_NE;
						}
					}
					return OP_LT;
			}
			return OP_NONE;
		}
		public boolean evaluate(boolean cmpResult) {
			switch (_code) {
				case NONE:
				case EQ:
					return cmpResult;
				case NE:
					return !cmpResult;
			}
			throw new RuntimeException("Cannot call boolean evaluate on non-equality operator '"
					+ _representation + "'");
		}
		public boolean evaluate(int cmpResult) {
			switch (_code) {
				case NONE:
				case EQ:
					return cmpResult == 0;
				case NE: return cmpResult != 0;
				case LT: return cmpResult <  0;
				case LE: return cmpResult <= 0;
				case GT: return cmpResult >  0;
				case GE: return cmpResult >= 0;
			}
			throw new RuntimeException("Cannot call boolean evaluate on non-equality operator '"
					+ _representation + "'");
		}
		public String toString() {
			StringBuffer sb = new StringBuffer(64);
			sb.append(getClass().getName());
			sb.append(" [").append(_representation).append("]");
			return sb.toString();
		}
		public String getRepresentation() {
			return _representation;
		}
	}

	private static abstract class MatcherBase implements I_MatchPredicate {
		private final CmpOp _operator;

		MatcherBase(CmpOp operator) {
			_operator = operator;
		}
		protected final int getCode() {
			return _operator.getCode();
		}
		protected final boolean evaluate(int cmpResult) {
			return _operator.evaluate(cmpResult);
		}
		protected final boolean evaluate(boolean cmpResult) {
			return _operator.evaluate(cmpResult);
		}
		@Override
		public final String toString() {
			StringBuffer sb = new StringBuffer(64);
			sb.append(getClass().getName()).append(" [");
			sb.append(_operator.getRepresentation());
			sb.append(getValueText());
			sb.append("]");
			return sb.toString();
		}
		protected abstract String getValueText();
	}

	static final class NumberMatcher extends MatcherBase {

		private final double _value;

		public NumberMatcher(double value, CmpOp operator) {
			super(operator);
			_value = value;
		}
		@Override
		protected String getValueText() {
			return String.valueOf(_value);
		}

		public boolean matches(ValueEval x) {
			double testValue;
			if(x instanceof StringEval) {
				// if the target(x) is a string, but parses as a number
				// it may still count as a match, only for the equality operator
				switch (getCode()) {
					case CmpOp.EQ:
					case CmpOp.NONE:
						break;
					case CmpOp.NE:
						// Always matches (inconsistent with above two cases).
						// for example '<>123' matches '123', '4', 'abc', etc
						return true;
					default:
						// never matches (also inconsistent with above three cases).
						// for example '>5' does not match '6',
						return false;
				}
				StringEval se = (StringEval)x;
				Double val = OperandResolver.parseDouble(se.getStringValue());
				if(val == null) {
					// x is text that is not a number
					return false;
				}
				return _value == val.doubleValue();
			} else if((x instanceof NumberEval)) {
				NumberEval ne = (NumberEval) x;
				testValue = ne.getNumberValue();
            } else if((x instanceof BlankEval)) {
                switch (getCode()) {
                    case CmpOp.NE:
                        // Excel counts blank values in range as not equal to any value. See Bugzilla 51498
                        return true;
                    default:
                        return false;
                }
			} else {
				return false;
			}
			return evaluate(Double.compare(testValue, _value));
		}
	}

	static final class BooleanMatcher extends MatcherBase {

		private final int _value;

		public BooleanMatcher(boolean value, CmpOp operator) {
			super(operator);
			_value = boolToInt(value);
		}
		@Override
		protected String getValueText() {
			return _value == 1 ? "TRUE" : "FALSE";
		}

		private static int boolToInt(boolean value) {
			return value ? 1 : 0;
		}

		public boolean matches(ValueEval x) {
			int testValue;
			if(x instanceof StringEval) {
				if (true) { // change to false to observe more intuitive behaviour
					// Note - Unlike with numbers, it seems that COUNTIF never matches
					// boolean values when the target(x) is a string
					return false;
				}
				@SuppressWarnings("unused")
				StringEval se = (StringEval)x;
				Boolean val = parseBoolean(se.getStringValue());
				if(val == null) {
					// x is text that is not a boolean
					return false;
				}
				testValue = boolToInt(val.booleanValue());
			} else if((x instanceof BoolEval)) {
				BoolEval be = (BoolEval) x;
				testValue = boolToInt(be.getBooleanValue());
            } else if((x instanceof BlankEval)) {
                switch (getCode()) {
                    case CmpOp.NE:
                        // Excel counts blank values in range as not equal to any value. See Bugzilla 51498
                        return true;
                    default:
                        return false;
                }
            } else if((x instanceof NumberEval)) {
                switch (getCode()) {
                    case CmpOp.NE:
                        // not-equals comparison of a number to boolean always returnes false
                        return true;
                    default:
                        return false;
                }
            } else {
				return false;
			}
			return evaluate(testValue - _value);
		}
	}

	public static final class ErrorMatcher extends MatcherBase {

		private final int _value;

		public ErrorMatcher(int errorCode, CmpOp operator) {
			super(operator);
			_value = errorCode;
		}
		@Override
		protected String getValueText() {
			return FormulaError.forInt(_value).getString();
		}

		public boolean matches(ValueEval x) {
			if(x instanceof ErrorEval) {
				int testValue = ((ErrorEval)x).getErrorCode();
				return evaluate(testValue - _value);
			}
			return false;
		}

		public int getValue() {
		    return _value;
		}
	}

	public static final class StringMatcher extends MatcherBase {

		private final String _value;
		private final Pattern _pattern;

		public StringMatcher(String value, CmpOp operator) {
			super(operator);
			_value = value;
			switch(operator.getCode()) {
				case CmpOp.NONE:
				case CmpOp.EQ:
				case CmpOp.NE:
					_pattern = getWildCardPattern(value);
					break;
				default:
					// pattern matching is never used for < > <= =>
					_pattern = null;
			}
		}
		@Override
		protected String getValueText() {
			if (_pattern == null) {
				return _value;
			}
			return _pattern.pattern();
		}

		public boolean matches(ValueEval x) {
			if (x instanceof BlankEval) {
				switch(getCode()) {
					case CmpOp.NONE:
					case CmpOp.EQ:
						return _value.length() == 0;
                    case CmpOp.NE:
                        // pred '<>' matches empty string but not blank cell
                        // pred '<>ABC'  matches blank and 'not ABC'
                        return _value.length() != 0;
				}
				// no other criteria matches a blank cell
				return false;
			}
			if(!(x instanceof StringEval)) {
				// must always be string
				// even if match str is wild, but contains only digits
				// e.g. '4*7', NumberEval(4567) does not match
				return false;
			}
			String testedValue = ((StringEval) x).getStringValue();
			if (testedValue.length() < 1 && _value.length() < 1) {
				// odd case: criteria '=' behaves differently to criteria ''

				switch(getCode()) {
					case CmpOp.NONE: return true;
					case CmpOp.EQ:   return false;
					case CmpOp.NE:   return true;
				}
				return false;
			}
			if (_pattern != null) {
				return evaluate(_pattern.matcher(testedValue).matches());
			}
            // String criteria in COUNTIF are case insensitive:
            // for example, the string "apples" and the string "APPLES" will match the same cells.
			return evaluate(testedValue.compareToIgnoreCase(_value));
		}
		/**
		 * Translates Excel countif wildcard strings into java regex strings
		 * @return <code>null</code> if the specified value contains no special wildcard characters.
		 */
		public static Pattern getWildCardPattern(String value) {
			int len = value.length();
			StringBuffer sb = new StringBuffer(len);
			boolean hasWildCard = false;
			for(int i=0; i<len; i++) {
				char ch = value.charAt(i);
				switch(ch) {
					case '?':  //Any single character
						hasWildCard = true;
						// match exactly one character
						sb.append('.');
						continue;
					case '*': //Zero or more characters
						hasWildCard = true;
						// match one or more occurrences of any character
						sb.append(".*");
						continue;
					case '~':
						if (i+1<len) {
							ch = value.charAt(i+1);
							switch (ch) {
								case '?':
								case '*':
									hasWildCard = true;
									sb.append('[').append(ch).append(']');
									i++; // Note - incrementing loop variable here
									continue;
							}
						}
						// else not '~?' or '~*'
						sb.append('~'); // just plain '~'
						continue;
					case '.':
					case '$':
					case '^':
					case '[':
					case ']':
					case '(':
					case ')':
						// escape literal characters that would have special meaning in regex
						sb.append("\\").append(ch);
						continue;
				}
				sb.append(ch);
			}
			if (hasWildCard) {
				return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
			}
			return null;
		}
	}
}
