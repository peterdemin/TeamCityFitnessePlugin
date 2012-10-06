package Fitnesse.agent.ResultReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler for XML parser which retrieves test results
 *
 * @author: elgris
 * @date 01.10.12
 */
class XmlResultParserHandler extends DefaultHandler {
    public static final String MESSAGE_COMPLETE = "Parsing completed";

    // name of XML node which contains counted statistics for given test
    private final String COUNTS_NODE = "counts";

    // name of XML node which contains count of correct (right) tests
    private final String RIGHTS_COUNT_NODE = "right";

    // name of XML node which contains count of erroneous (wrong) tests
    private final String WRONGS_COUNT_NODE = "wrong";

    // name of XML node which contains count of ignored tests
    private final String IGNORES_COUNT_NODE = "ignores";

    // name of XML node which contains count of tests with exceptions
    private final String EXCEPTIONS_COUNT_NODE = "exceptions";

    // name of XML node which contains amount of consumed time
    private final String TIME_CONSUMED_NODE = "runTimeInMillis";

    /**
     * Flag determines that we currently parse XML document inside 'count' node
     */
    private boolean isInsideCountsNode;

    /**
     * Parsed integer value from some parseable result node
     */
    private int parsedValue;

    private String currentNode;

    private Result result;

    public Result getCollectedResult() {
        return result;
    }

    public void startDocument() throws org.xml.sax.SAXException {
        result = new Result();
        isInsideCountsNode = false;
        parsedValue = 0;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentNode = qName;
        isInsideCountsNode = isInsideCountsNode || currentNode.equalsIgnoreCase(COUNTS_NODE);
    }

    public void characters(char chars[], int start, int length) throws SAXException {
        if(isCurrentNodeParseable()) {
            try {
                parsedValue = Integer.parseInt(new String(chars, start, length));
            } catch (NumberFormatException e) {
                parsedValue = 0;
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isInsideCountsNode) {
            if (qName.equalsIgnoreCase(RIGHTS_COUNT_NODE)) {
                result.setRightsCount(parsedValue);
            } else if (qName.equalsIgnoreCase(WRONGS_COUNT_NODE)) {
                result.setWrongsCount(parsedValue);
            } else if (qName.equalsIgnoreCase(IGNORES_COUNT_NODE)) {
                result.setIgnoresCount(parsedValue);
            } else if (qName.equalsIgnoreCase(EXCEPTIONS_COUNT_NODE)) {
                result.setExceptionsCount(parsedValue);
            }
        }
        if(qName.equalsIgnoreCase(TIME_CONSUMED_NODE)) {
            result.setTimeConsumed(parsedValue);
            /**
             * Notice that here we stop parsing because TIME_CONSUMED_NODE should be last node we need to parse.
             * After that we don't need to proceed, let's save our time.
             */
            throw new SAXException(MESSAGE_COMPLETE);
        }
        isInsideCountsNode = isInsideCountsNode && !currentNode.equalsIgnoreCase(COUNTS_NODE);
    }

    private boolean isCurrentNodeParseable() {
        return currentNode.equalsIgnoreCase(RIGHTS_COUNT_NODE)
            || currentNode.equalsIgnoreCase(WRONGS_COUNT_NODE)
            || currentNode.equalsIgnoreCase(IGNORES_COUNT_NODE)
            || currentNode.equalsIgnoreCase(EXCEPTIONS_COUNT_NODE)
            || currentNode.equalsIgnoreCase(TIME_CONSUMED_NODE)
            ;
    }
}
