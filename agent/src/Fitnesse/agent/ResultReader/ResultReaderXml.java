package Fitnesse.agent.ResultReader;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

/**
 * Result reader. Uses stream connected to some XML source
 *
 * @author: elgris
 * @date 30.09.12
 */
class ResultReaderXml implements ResultReader {
    public Result getResult(InputStream stream) throws InvalidSourceException {
        XmlResultParserHandler handler = new XmlResultParserHandler();
        try {
            SAXParser parser = getParser();
            parser.parse(stream, handler);
        } catch (SAXException e) {
            if (!e.getMessage().equalsIgnoreCase(XmlResultParserHandler.MESSAGE_COMPLETE)) {
                throw new InvalidSourceException(e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new InvalidSourceException(e.getMessage(), e);
        }
        return handler.getCollectedResult();
    }

    private SAXParser getParser() throws SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        return factory.newSAXParser();
    }
}
