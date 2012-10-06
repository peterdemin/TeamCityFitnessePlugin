package Fitnesse.agent.ResultReader;

/**
 * Exception is thrown when reader tries to read some invalid source
 * (e.g. XML source is invalid, text source contains errors, etc)
 *
 * @author: elgris
 * @date 30.09.12
 */
public class InvalidSourceException extends Exception {
    public InvalidSourceException() {
        super();
    }

    public InvalidSourceException(java.lang.String s) {
        super(s);

    }

    public InvalidSourceException(java.lang.String s, Throwable throwable) {
        super(s,throwable);
    }
}
