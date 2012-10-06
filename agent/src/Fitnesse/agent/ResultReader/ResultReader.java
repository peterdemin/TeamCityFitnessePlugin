package Fitnesse.agent.ResultReader;

import java.io.InputStream;

/**
 * Result reader interface. Can only get results from incoming data stream
 *
 * @author: elgris
 * @date 30.09.12
 */
public interface ResultReader {
    public Result getResult(InputStream stream) throws InvalidSourceException;
}
