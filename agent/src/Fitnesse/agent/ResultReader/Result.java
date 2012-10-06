package Fitnesse.agent.ResultReader;

/**
 * Result data object. Represents result of test running: right, wrong, ignored tests count
 * as well as exceptions count
 *
 * @author: elgris
 * @date 30.09.12
 */
public class Result {
    private int wrongsCount = 0;
    private int rightsCount = 0;
    private int ignoresCount = 0;
    private int exceptionsCount = 0;

    private int timeConsumed = 0;   // consumed time in millisecs

    public int getWrongsCount() {
        return wrongsCount;
    }

    public void setWrongsCount(int wrongsCount) {
        this.wrongsCount = wrongsCount;
    }

    public int getRightsCount() {
        return rightsCount;
    }

    public void setRightsCount(int rightsCount) {
        this.rightsCount = rightsCount;
    }

    public int getIgnoresCount() {
        return ignoresCount;
    }

    public void setIgnoresCount(int ignoresCount) {
        this.ignoresCount = ignoresCount;
    }

    public int getExceptionsCount() {
        return exceptionsCount;
    }

    public void setExceptionsCount(int exceptionsCount) {
        this.exceptionsCount = exceptionsCount;
    }

    public int getTimeConsumed() {
        return timeConsumed;
    }

    public void setTimeConsumed(int timeConsumed) {
        this.timeConsumed = timeConsumed;
    }
}
