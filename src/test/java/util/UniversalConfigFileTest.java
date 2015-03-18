package util;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import bean.Issue;
import org.junit.Test;


public class UniversalConfigFileTest {
    @Test
    public void コンストラクタのテスト() throws IOException {
        URL resource = IssueLoader.class.getResource("/issues.xml");
        IssueLoader issueLoader = new IssueLoader();
        Map<String, List<Issue>> issueMap = issueLoader.load(resource.getPath());
        for(Issue issue : issueMap.get("")) {
            System.out.println(issue.getId());
            System.out.println(issue.getName());
        }

    }

}