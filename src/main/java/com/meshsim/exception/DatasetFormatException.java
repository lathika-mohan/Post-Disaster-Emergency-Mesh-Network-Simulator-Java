package com.meshsim.exception;

/** Thrown by the CSV loader when a row is malformed. */
public class DatasetFormatException extends MeshSimulationException {

    private final int lineNumber;
    private final String offendingToken;

    public DatasetFormatException(int lineNumber, String offendingToken, String reason) {
        super("Dataset format error at line " + lineNumber + " (token '" + offendingToken
                + "'): " + reason);
        this.lineNumber = lineNumber;
        this.offendingToken = offendingToken;
    }

    public int lineNumber() {
        return lineNumber;
    }

    public String offendingToken() {
        return offendingToken;
    }
}
