package util;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import bean.Issue;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 国際化対応したコンフィグファイルクラス
 */
public class IssueLoader {

    public IssueLoader() {}

    /**
     * プロパティファイルを生成・ロードする。
     * @param path プロパティファイルパス
     */
    public Map<String, List<Issue>> load(final String path) throws IOException {
        //ファイルを取得
        File file = new File(path);
        if(!file.exists()){
            throw new FileNotFoundException(path);
        }
        XMLParse xmlParse = new XMLParse();
        try {
            FileInputStream is = new FileInputStream(file);
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            // XMLファイルを指定されたデフォルトハンドラーで処理
            parser.parse(new InputSource(is), xmlParse);
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        return xmlParse.condition;
    }

    /**
     * XMLのパーサ
     */
    public class XMLParse extends DefaultHandler {
        private Locator locator;
        private List<Issue> statues = new ArrayList<>();
        private List<Issue> stories = new ArrayList<>();
        private boolean isStatusIssue = false;
        private boolean isStoryIssue = false;
        private Map<String, List<Issue>> condition;

        @Override
        public void setDocumentLocator(final Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
            if (isStatusIssue && qName.equalsIgnoreCase("issue")) {
                Issue status = new Issue();
                status.setId(Integer.parseInt(attributes.getValue("id")));
                status.setName(attributes.getValue("name"));
                statues.add(status);
            } else if (isStoryIssue && qName.equalsIgnoreCase("issue")) {
                Issue story = new Issue();
                story.setId(Integer.parseInt(attributes.getValue("id")));
                story.setName(attributes.getValue("name"));
                stories.add(story);
            } else if (qName.equalsIgnoreCase("status")) {
                isStatusIssue = true;
            } else if (qName.equalsIgnoreCase("story")) {
                isStoryIssue = true;
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) {
            if (statues != null && qName.equalsIgnoreCase("status")) {
                condition.put("STATUS", statues);
                isStatusIssue = false;
            } else if (stories != null && qName.equalsIgnoreCase("story")) {
                condition.put("STORY", stories);
                isStatusIssue = false;
            }
        }
    }
}
