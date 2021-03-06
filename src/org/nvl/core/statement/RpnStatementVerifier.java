package org.nvl.core.statement;

import org.nvl.core.rpn.RpnVerifier;
import org.nvl.core.rpn.verifier.ArrayRpnVerifier;
import org.nvl.core.rpn.verifier.BooleanRpnVerifier;
import org.nvl.core.rpn.verifier.NumberRpnVerifier;
import org.nvl.core.rpn.verifier.StringRpnVerifier;
import org.nvl.core.variable.manager.VariableManager;

import static org.nvl.MessageConstants.INVALID_INPUT_FORMAT;

public class RpnStatementVerifier implements StatementVerifier {

    private VariableManager variableManager;
    private boolean isBooleanOperation;
    private boolean isStringOperation;

    public boolean isBooleanOperation() {
        return isBooleanOperation;
    }

    public boolean isStringOperation() {
        return isStringOperation;
    }

    public boolean isArrayOperation() {
        return isArrayOperation;
    }

    private boolean isArrayOperation;
    private StringBuilder valueStatement;

    public RpnStatementVerifier(VariableManager variableManager) {
        this.variableManager = variableManager;
    }

    @Override
    public boolean verifyStatement(String statement) {
        checkType(statement);

        RpnVerifier verify;

        if (isStringOperation) {        //we have string operations
            verify = new StringRpnVerifier();       //we verify the statement
            return verify.correct(valueStatement);
        }
        if (isArrayOperation) {
            verify = new ArrayRpnVerifier();
            return verify.correct(valueStatement);
        }
        if (isBooleanOperation) {                     //we have boolean operations
            verify = new BooleanRpnVerifier();
            return verify.correct(valueStatement);
        }
        verify = new NumberRpnVerifier();           //we have number operations
        return verify.correct(valueStatement);
    }

    public void checkType(String statement) {
        valueStatement = new StringBuilder(statement);
        isBooleanOperation = false;
        isStringOperation = false;
        isArrayOperation = false;
        for (int i = 0; i < valueStatement.length(); ++i) {

            char character = valueStatement.charAt(i);

            if (character == '{') {           //if we have opening bracket for array
                isArrayOperation = true;        //we leave the input like that
                do {
                    i++;
                    character = valueStatement.charAt(i);
                } while (character != '}');       //so we iterate while we reach the closing bracket
                continue;
            }

            if (character == '\'') {            //if we have quotes in the statement than we have strings and string operations, so we leave it like that
                isStringOperation = true;
                do {
                    i++;
                    character = valueStatement.charAt(i);
                } while (character != '\'');        //iterate through the input until we get past the string
                continue;
            }

            if (character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z') {   //change variable with its value
                String variable = String.valueOf(character);
                if (variable.startsWith("t") || variable.startsWith("T")) {             //if we find t or T
                    if (valueStatement.substring(i, i + 4).equalsIgnoreCase("true")) {      //if its the keyword TRUE, we leave it like that
                        i = i + 3;                                                          //so we increment i
                        isBooleanOperation = true;                                           //and we have boolean operations
                        continue;
                    }
                }
                if (variable.startsWith("f") || variable.startsWith("F")) {                   //if we find f or F
                    if (valueStatement.substring(i, i + 5).equalsIgnoreCase("false")) {     //and it is from the keyword FALSE, we leave it like that
                        i = i + 4;                                                          //so we increment i
                        isBooleanOperation = true;                                          //and we have boolean operations
                        continue;
                    }
                }
                if (!variableManager.containsVariable(variable)) {                      //if the variable is not declared
                    throw new RuntimeException(String.format(INVALID_INPUT_FORMAT, statement, "Verification error"));
                }
                valueStatement.deleteCharAt(i);                                             //remove the variable from the resulted string
                valueStatement.insert(i, variableManager.getVariable(variable).getValue());     //and add its value
                i--;                                                                    //return one index to check the value
            }
        }
    }
}
