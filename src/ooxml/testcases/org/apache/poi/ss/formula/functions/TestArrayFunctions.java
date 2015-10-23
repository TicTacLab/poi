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

import junit.framework.TestCase;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Tests {@link WorkbookEvaluator}.
 *
 * @author Josh Micich
 */
public class TestArrayFunctions extends TestCase {

    XSSFWorkbook wb;

    @Override
    protected void setUp() throws Exception {
        wb = XSSFTestDataSamples.openSampleWorkbook("TestArrayAggregations.xlsx");
    }

    public void setArrayFormula(Workbook wb, String formula) {
        wb.getSheet("Sheet1").setArrayFormula(formula, CellRangeAddress.valueOf("F18:F18"));
    }

    public CellValue evaluate(Workbook wb) {
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        fe.setDebugEvaluationOutputForNextEval(true);
        return fe.evaluate(wb.getSheet("Sheet1").getRow(17).getCell(5));
    }

    public String int2type(int type) {
        switch (type) {
            case 0: return "NUMBER";
            case 1: return "STRING";
            case 2: return "FORMULA";
            case 3: return "BLANK";
            case 4: return "BOOLEAN";
            case 5: return "ERROR";
        }
        return "42";
    }

    public void assertType(int expected, CellValue value) {
        int actual = value.getCellType();
        assertEquals( String.format("Expect type \"%s\", but get \"%s\"", int2type(expected), int2type(actual)),
                expected, actual);
    }

    public void assertFormulaResult(Workbook wb, double expected, String formula) {
        setArrayFormula(wb, formula);
        CellValue value = evaluate(wb);
        assertType(Cell.CELL_TYPE_NUMERIC, value);
        assertEquals(expected, value.getNumberValue(), 0.01);
    }

    public void assertFormulaResult(Workbook wb, boolean expected, String formula) {
        setArrayFormula(wb, formula);
        CellValue value = evaluate(wb);
        assertType(Cell.CELL_TYPE_BOOLEAN, value);
        assertEquals(expected, value.getBooleanValue());
    }

    public void assertFormulaResult(Workbook wb, String expected, String formula) {
        setArrayFormula(wb, formula);
        CellValue value = evaluate(wb);
        assertType(Cell.CELL_TYPE_STRING, value);
        assertEquals(expected, value.getStringValue());
    }

    public void assertFormulaResult(Workbook wb, ErrorEval expected, String formula) {
        setArrayFormula(wb, formula);
        CellValue value = evaluate(wb);
        assertType(Cell.CELL_TYPE_ERROR, value);
        assertEquals(expected, ErrorEval.valueOf(value.getErrorValue()));
    }

    public void testOperators() {
        assertFormulaResult(wb, 10500, "D2:D17*E2:E17");
    }
    
    public void testMONTH() {assertFormulaResult(wb, 1, "MONTH(D2:D17)");}

    public void testNA() {assertFormulaResult(wb, ErrorEval.NA, "NA()");}

    public void testNOW() {assertFormulaResult(wb, 0, "NOW()-NOW()");}

    public void testODD() {assertFormulaResult(wb, 10501, "ODD(F2:F17)");}

    public void testPERCENTILE() {
        assertFormulaResult(wb, 2050, "PERCENTILE(E2:E17, 0.5)");
        assertFormulaResult(wb, 2050, "PERCENTILE(E2:E17, D2:D17/10)");
    }

    public void testPI() {assertFormulaResult(wb, 3.14, "PI()");}

    public void testPOISSON() {
        assertFormulaResult(wb, 0.04, "POISSON(A2, D2, TRUE)");
        assertFormulaResult(wb, 0.04, "POISSON(A2, D2:D17, TRUE)");
        assertFormulaResult(wb, 0.04, "POISSON(A2:A17, D2, TRUE)");
        assertFormulaResult(wb, 0.04, "POISSON(A2:A17, D2:D17, TRUE)");
        assertFormulaResult(wb, 0.04, "POISSON(A2:A17, D2:D17, IF(MOD(A2:A17, 2)=0, TRUE, FALSE))");
    }

    public void testPOWER() {
        assertFormulaResult(wb, 4,  "POWER(2, 2)");
        assertFormulaResult(wb, 32, "POWER(2, D2:D17)");
        assertFormulaResult(wb, 1,  "POWER(A2:A17, 2)");
        assertFormulaResult(wb, 1, "POWER(A2:A17, D2:D17)");
    }

    public void testPRODUCT() {
        assertFormulaResult(wb, 2,   "PRODUCT(2)");
        assertFormulaResult(wb, 24,  "PRODUCT(A2:A5)");
        assertFormulaResult(wb, 4, "PRODUCT(2, 2)");
        assertFormulaResult(wb, 48, "PRODUCT(A2:A5, 2)");
        assertFormulaResult(wb, 720, "PRODUCT(A2:A4, D2:D4)");
    }

    public void testPROPER() {assertFormulaResult(wb, "Sedan", "PROPER(C2:C17)");}

    public void testRADIANS() {assertFormulaResult(wb, 0.02, "RADIANS(A2:A17)");}

    public void testRAND() {assertFormulaResult(wb, 1, "ROUNDUP(RAND(), 0)");}

    public void testRANK() {
        assertFormulaResult(wb, 10,  "SUM(RANK(10500, F2:F17))");
        assertFormulaResult(wb, 135, "SUM(RANK(F2:F17, F2:F17, 0))");
        assertFormulaResult(wb, 154, "SUM(RANK(F2:F17, F2:F17, MOD(A2:A17,2)))");
    }

    public void testREPT() {
        assertFormulaResult(wb, "xxxxx", "REPT(\"x\", D2:D17)");
        assertFormulaResult(wb, "SedanSedan", "REPT(C2:C17, 2)");
        assertFormulaResult(wb, "Sedan", "REPT(C2:C17, A2:A17)");
    }

    public void testROUND_ROUNDUP_ROUNDDOWN() {
        assertFormulaResult(wb, 10500, "ROUND(F2:F17, 5)");
        assertFormulaResult(wb, 1,     "ROUND(1, D2:D17)");
        assertFormulaResult(wb, 10500, "ROUND(F2:F17, D2:D17)");
        assertFormulaResult(wb, 10500, "ROUNDUP(F2:F17, 5)");
        assertFormulaResult(wb, 1,     "ROUNDUP(1, D2:D17)");
        assertFormulaResult(wb, 10500, "ROUNDUP(F2:F17, D2:D17)");
        assertFormulaResult(wb, 10500, "ROUNDDOWN(F2:F17, 5)");
        assertFormulaResult(wb, 1, "ROUNDDOWN(1, D2:D17)");
        assertFormulaResult(wb, 10500, "ROUNDDOWN(F2:F17, D2:D17)");
    }

    public void testROW() {
        // TODO: row can take array is an argument,
        // but if it typed as ARRAY formula, then it return array
        assertFormulaResult(wb, 152, "SUM(ROW(F2:F17))");
    }


    public void testCOLUMN() {
        //assertFormulaResult(wb, 10, "SUM(COLUMN(A2:D17))");
    }

    public void testCOMPLEX() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "COMPLEX(A2:A17,F2:F17)");
    }

    public void testROWS() {assertFormulaResult(wb, 16, "ROWS(F2:F17)");}

    public void testSEARCH() {
        assertFormulaResult(wb, 16, "SUM(SEARCH(C2:C17, C2:C17))");
        assertFormulaResult(wb, 56, "SUM(SEARCH(\"e\", C2:C17, 1+MOD(D2:D17, 2)))");
    }

    public void testSECOND() {assertFormulaResult(wb, 0, "SECOND(F2:F17)");}

    public void testSIGN() {assertFormulaResult(wb, 1.0, "SIGN(F2:F17)");}

    public void testSIN() {assertFormulaResult(wb, 1, "ASIN(SIN(A2:A17))");}

    public void testSINH() {assertFormulaResult(wb, 1, "ASINH(SINH(A2:A17))");}

    public void testSLOPE() {assertFormulaResult(wb, 0.51, "SLOPE(A2:A17,D2:D17)");}

    public void testSMALL() {
        assertFormulaResult(wb, 1.0, "SMALL(A2:A17,A2:A17)");
        assertFormulaResult(wb, 4, "SMALL(IF(B2:B17=\"Ingle\", ROW(B2:B17), \"\"), 1)");
    }

    public void testSQRT() {assertFormulaResult(wb, 1, "SQRT(A2:A17)");}

    public void testSTDEV() {assertFormulaResult(wb, 4.76, "STDEV(A2:A17)");}

    public void testSUMIF() {assertFormulaResult(wb, 86, "SUMIF(A2:A17, \">\"&TEXT(A2:A17,\"0\"),D2:D17)");}



    public void testSUMPRODUCT() {assertFormulaResult(wb, 813, "SUMPRODUCT(A2:A17, D2:D17)");}

    public void testSUMSQ() {assertFormulaResult(wb, 2091, "SUMSQ(A2:A17, D2:D17)");}

    public void testSUMX2MY2() {assertFormulaResult(wb, 901, "SUMX2MY2(A2:A17,D2:D17)");}

    public void testSUMX2PY2() {assertFormulaResult(wb, 2091, "SUMX2PY2(A2:A17, D2:D17)");}

    public void testSUMXMY2() {assertFormulaResult(wb, 465, "SUMXMY2(A2:A17, D2:D17)");}

    public void testT() {assertFormulaResult(wb, "Sedan", "T(C2:C17)");}

    public void testTAN() {assertFormulaResult(wb, 5, "DEGREES(ATAN(TAN(RADIANS(D2:D17))))");}

    public void testTANH() {assertFormulaResult(wb, 5.0, "ATANH(TANH(D2:D17))");}

    public void testTEXT() {assertFormulaResult(wb, "10500", "TEXT(F2:F17, \"0\")");}

    public void testTIME() {assertFormulaResult(wb, 1, "HOUR(TIME(A2:A17, D2:D17, A2:A17))");}

    public void testTODAY() {assertFormulaResult(wb, 0, "TODAY()-TODAY()");}

    public void testTRIM() {assertFormulaResult(wb, "Sedan", "TRIM(C2:C17)");}

    public void testTRUE() {assertFormulaResult(wb, true, "TRUE()");}

    public void testUPPER() {assertFormulaResult(wb, "SEDAN", "UPPER(C2:C17)");}

    public void testVALUE() {assertFormulaResult(wb, 10500, "VALUE(TEXT(F2:F17, \"0\"))");}

    public void testVAR() {assertFormulaResult(wb, 24877000, "VAR(F2:F17)");}

    public void testVARP() {assertFormulaResult(wb, 23322187.5, "VARP(F2:F17)");}

    public void testVLOOKUP() {assertFormulaResult(wb, 2100, "VLOOKUP(A2:A17, A2:F17, 5)");}

    public void testYEAR() {assertFormulaResult(wb, 1928, "YEAR(F2:F17)");}

	public void testAggregateOfBasicOperators() {
        assertFormulaResult(wb, 189691.0, "SUM(D2:D17+F2:F17)");
        assertFormulaResult(wb, -189509.0, "SUM(D2:D17-F2:F17)");
        assertFormulaResult(wb, 1242300.0, "SUM(D2:D17*F2:F17)");
        assertFormulaResult(wb, 0.007894, "SUM(D2:D17/F2:F17)");
        assertFormulaResult(wb, 316674713773269.00, "SUM(D2:D17^A2:A17)");
	}

    public void testAggregateFunctions() {
        assertFormulaResult(wb, -4790016000.0, "PRODUCT(A2:A17-D2:D17)");
    }

    public void testThreeAndMoreArguments() {
        assertFormulaResult(wb, 189827.0, "SUM(A2:A17+D2:D17+F2:F17)");
        assertFormulaResult(wb, 222677.0, "SUM(A2:A17+D2:D17+F2:F17+E2:E17)");
        //        assertFormulaResult(wb, 189827.0, "{1,2,3}*3");
    }

    public void testLogicOperators() {
        assertFormulaResult(wb, true, "OR(C2:C17=\"Sedan\")");
        assertFormulaResult(wb, true, "OR(NOT(C2:C17=\"Sedan\"))");
        //assertFormulaResult(wb, 0, "IF(C2<>\"Sedan\",1,0)");
        //assertFormulaResult(wb, 57600.0, "SUM(IF(C2:C17=\"Sedan\", INDEX(F2:F17, ROW(F2:F17), 1), \"\"))");
    }

    public void testIFERROR() {
        assertFormulaResult(wb, -2813.51, "SUM(IFERROR(F2:F17/(D2:D17-2*A2:A17), E2:E17))");
    }

    public void testIMREAL() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "IMREAL(F2:F17)");
    }


    public void testFixed1ArgFunction() {
        assertFormulaResult(wb, -189600,"SUM(-F2:F17)");
        assertFormulaResult(wb, "\u0001", "CHAR(F2:F17/F2:F17)");
        assertFormulaResult(wb, 105.0, "F2:F17%");
    }

    public void testFixed2ArgFunction() {
        assertFormulaResult(wb, "I", "ROMAN(F2:F17/F2:F17,0)");
    }

    public void testFixed3ArgFunction() {
        assertFormulaResult(wb, "10", "MID(F2:F17,1,2)");
    }

    public void testFixed4ArgFunction() {
        assertFormulaResult(wb, "Mu500", "REPLACE(F2:F17,1,2,\"Mu\")");
    }

    public void testVar1or2ArgFunction() {
        assertFormulaResult(wb, 1.0, "TRUNC(F2:F17/F2:F17,1)");
    }

    public void testVar2or3ArgFunction() {
        assertFormulaResult(wb, 1, "SEARCH(A2:A17,F2:F17)");
    }

    public void testVar3or4ArgFunction() {
        assertFormulaResult(wb, "105Do0", "SUBSTITUTE(F2:F17,\"0\",\"Do\",2)");
    }

    public void testIf() {
        assertFormulaResult(wb, 2, "SUM(IF(F2:F17=10500, A2:A17+A2:A17, 0))");
    }

    public void testChoose() {
        assertFormulaResult(wb, "Barnhill", "CHOOSE(A2:A17,B2:B17,C2:C17,D2:D17,E2:E17,F2:F17)");
    }

    public void testTextFunction() {
        assertFormulaResult(wb, "Sedan1", "CONCATENATE(C2:C17,A2:A17)");
        assertFormulaResult(wb, 5, "LEN(F2:F17)");
        assertFormulaResult(wb, "00", "RIGHT(F2:F17,2)");
    }
    public void testFreeRefFunction() {
        assertFormulaResult(wb, "Total Sales", "INDIRECT(CONCATENATE(\"f\", A2:A17))");
    }

    public void testAddress() {
        assertFormulaResult(wb, "Sedan!$E$1", "ADDRESS(A2:A17,D2:D17,1,1,C2:C17)");

    }

    public void testFactDouble() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "FACTDOUBLE(D2:D17)");
    }
    public void testFact() {
        assertFormulaResult(wb, 120, "FACT(D2:D17)");
    }

    public void testRANDBETWEEN() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "RANDBETWEEN(A2:A17,A2:A17)");
    }

    public void testISEVENT() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "ISEVEN(A2:A17)");
    }
    public void testISODD() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "ISODD(A2:A17)");
    }
    public void testNETWORKDAYS() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "NETWORKDAYS(G2:G17,G2:G17)");
    }
    public void testEDATE() {
        assertFormulaResult(wb, 2979147, "SUM(EDATE(DATE(E2:E17,D2:D17,A2:A17),A2:A17-A2:A17))");
    }

    public void testOffset() {
        assertFormulaResult(wb, 17700.0, "SUM(OFFSET(F2:F17,0,0,2))");
    }

    public void testWeekdayFunc()  {
        assertFormulaResult(wb, 68, "SUM(WEEKDAY(F2:F17, A2:A17-A2:A17+1))");
    }

    public void testABSFunc() {assertFormulaResult(wb, 10500, "ABS(F2:F17)");}

    public void testACOSFunc() {assertFormulaResult(wb, 1.57, "ACOS(F2:F17-F2:F17)");}

    public void testWORKDAY() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "WORKDAY(G2:G17,A2:A17)");
    }

    public void testYEARFRAC() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "YEARFRAC(G2:G17,G2:G17)");
    }

    public void testEOMONTH() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "EOMONTH(G2:G17,A2:A17)");
    }

    public void testWEEKNUM() {
        assertFormulaResult(wb, ErrorEval.VALUE_INVALID, "WEEKNUM(F2:F17)");
    }

    public void testMROUND() {
        assertFormulaResult(wb, 36501, "SUM(MROUND(F2:F17/A2:A17, 3))");
    }

    public void testACOSHFunc() {assertFormulaResult(wb, 9.95, "ACOSH(F2:F17)");}

    public void testANDFunc() {assertFormulaResult(wb, true, "AND(F2:F17)");}

    public void testASINFunc() {assertFormulaResult(wb, 0, "ASIN(F2:F17-F2:F17)");}

    public void testASINHFunc() {assertFormulaResult(wb, 9.95, "ASINH(F2:F17)");}

    public void testATANFunc() {assertFormulaResult(wb, 1.57, "ATAN(F2:F17)");}

    public void testATAN2Func() {assertFormulaResult(wb, 0.78, "ATAN2(F2:F17,F2:F17)");}

    public void testATANHFunc() {assertFormulaResult(wb, 0, "ATANH(F2:F17-F2:F17)");}

    public void testAVEDEV() {assertFormulaResult(wb, 4137.5, "AVEDEV(F2:F17)");}

    public void testAVERAGE() {assertFormulaResult(wb, 11850, "AVERAGE(F2:F17)");}

    public void testCEILING() {assertFormulaResult(wb, 10500, "CEILING(F2:F17, 1)");}

    //public void testBINOMDIST() {assertFormulaResult(wb, 1, "BINOM.DIST(F2:F17, F2:F17, 0.5, 1)");}

    public void testCHAR() {assertFormulaResult(wb, ErrorEval.VALUE_INVALID , "CHAR(F2:F17)");}

    public void testCLEAN() {assertFormulaResult(wb, "10500", "CLEAN(F2:F17)");}

    public void testCODE() {assertFormulaResult(wb, "49", "CODE(F2:F17)");}

    public void testCOLUMNS() {assertFormulaResult(wb, 1, "COLUMNS(F2:F17)");}

    public void testCOS() {assertFormulaResult(wb, 0.69, "COS(F2:F17)");}

    public void testCOSH() {assertFormulaResult(wb, 1, "COSH(F2:F17-F2:F17)");}

    public void testCOUNT() {assertFormulaResult(wb, 16, "COUNT(F2:F17)");}

    public void testCOUNTA() {assertFormulaResult(wb, 32, "COUNTA(F2:F17,A2:A17)");}

    public void testCOUNTBLANK() {assertFormulaResult(wb, 0, "COUNTBLANK(F2:F17)");}

    public void testCOUNTIF() {assertFormulaResult(wb, 1, "COUNTIF(F2:F17, F2:F17)");}

    public void testDATE() {assertFormulaResult(wb, 487, "DATE(A2:A17,D2:D17,A2:A17)");}

    public void testDAY() {assertFormulaResult(wb, 29, "DAY(F2:F17)");}

    public void testDAYS360() {assertFormulaResult(wb, 0, "DAYS360(F2:F17, F2:F17)");}

    public void testDEGREES() {assertFormulaResult(wb, 601605.68, "DEGREES(F2:F17)");}

    public void testDEVSQ() {assertFormulaResult(wb, 373155000, "DEVSQ(F2:F17)");}

    public void testDOLLAR() {assertFormulaResult(wb, 10500, "DOLLAR(F2:F17)");}

    public void testERRORTYPE() {assertFormulaResult(wb, 2, "ERROR.TYPE(SUM(F2:F17)/0)");}

    public void testEVEN() {assertFormulaResult(wb, 10500, "EVEN(F2:F17)");}

    public void testEXACT() {assertFormulaResult(wb, true, "EXACT(F2:F17, F2:F17)");}

    public void testFIND() {assertFormulaResult(wb, 1, "FIND(F2:F17, F2:F17)");}

    public void testFIXED() {assertFormulaResult(wb, "10,500.0", "FIXED(F2:F17, A2:A17)");}

    public void testFLOOR() {assertFormulaResult(wb, 10500, "FLOOR(F2:F17, 2)");}

    public void testHLOOKUP() {assertFormulaResult(wb, 13800, "HLOOKUP(10500,A2:F17,3,TRUE)");}

    public void testHOUR() {assertFormulaResult(wb, 0, "HOUR(F2:F17)");}

    public void testINDEX() {
        //assertFormulaResult(wb, 7200, "INDEX(F2:F17, A3:A18)");
        assertFormulaResult(wb, 13600, "INDEX(F2:F17, SMALL(IF(B2:B17=\"Ingle\", ROW(B2:B17), \"\"), 1))");
    }

    public void testINT() {assertFormulaResult(wb, 10500, "INT(F2:F17)");}

    public void testISBLANK() {assertFormulaResult(wb, false, "ISBLANK(F2:F17)");}

    public void testISERR() {assertFormulaResult(wb, false, "ISERR(F2:F17)");}

    public void testISERROR() {assertFormulaResult(wb, false, "ISERROR(F2:F17)");}

    public void testISLOGICAL() {assertFormulaResult(wb, false, "ISLOGICAL(F2:F17)");}

    public void testISNA() {assertFormulaResult(wb, false, "ISNA(F2:F17)");}

    public void testISNONTEXT() {assertFormulaResult(wb, true, "ISNONTEXT(F2:F17)");}

    public void testISNUMBER() {assertFormulaResult(wb, true, "ISNUMBER(F2:F17)");}

    public void testISREF() {assertFormulaResult(wb, true, "ISREF(F2:F17)");}

    public void testISTEXT() {assertFormulaResult(wb, false, "ISTEXT(F2:F17)");}

    public void testLARGE() {
        assertFormulaResult(wb, 19350, "LARGE(F2:F17,1)");
        assertFormulaResult(wb, 18400, "LARGE(F2:F17,2)");
        assertFormulaResult(wb, 16800, "LARGE(F2:F17,3)");
    }

    public void testLEFT() {assertFormulaResult(wb, "105", "LEFT(F2:F17,3)");}

    public void testLN() {assertFormulaResult(wb, 9.25, "LN(F2:F17)");}

    public void testLOG() {assertFormulaResult(wb, 13.35, "LOG(F2:F17,2)");}

    public void testLOG10() {assertFormulaResult(wb, 4.02, "LOG10(F2:F17)");}

    public void testLOOKUP() {assertFormulaResult(wb, 12, "LOOKUP(13300,F2:F17,A2:A17)");}

    public void testLOWER() {assertFormulaResult(wb, "10500", "LOWER(F2:F17)");}

    public void testMATCH() {assertFormulaResult(wb, 12, "MATCH(13300,F2:F17,0)");}

    public void testMAX() {assertFormulaResult(wb, 19350, "MAX(F2:F17)");}

    public void testMAXA() {assertFormulaResult(wb, 19350, "MAXA(F2:F17)");}

    public void testMEDIAN() {assertFormulaResult(wb, 13400, "MEDIAN(F2:F17)");}

    public void testMIN() {assertFormulaResult(wb, 1600, "MIN(F2:F17)");}

    public void testMINA() {assertFormulaResult(wb, 1600, "MINA(F2:F17)");}

    public void testMINUTE() {assertFormulaResult(wb, 0, "MINUTE(F2:F17)");}

    public void testMOD() {assertFormulaResult(wb, 0, "MOD(F2:F17, 2)");}

    public void testMODE() {assertFormulaResult(wb, 6000, "MODE(F2:F17)");}

}
