import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;

import java.util.List;

/**
 * Created by uggds.
 */
public class RedmineController {
    private static final String URI = "";
    private static final String API_ACCESS_KEY = "";
    private static final String PROJECT_KEY = "";
    private static Integer QUERY_ID = null;

    public static void main(String[] args) throws Exception {

        IssueManager issueManager = RedmineManagerFactory.createWithApiKey(URI, API_ACCESS_KEY).getIssueManager();

        // かんばんタスクチケットリスト
        List<Issue> issues = issueManager.getIssues(PROJECT_KEY, QUERY_ID);
        int size = issues.size();
        for (Issue issue : issues) {
            Integer rootId = Integer.valueOf(issue.getSubject().substring(1, 5));

            if (isInvalidFormat(rootId)) {
                System.out.println("NG format ticket. [" + issue.getId() + " : " + issue.getSubject() + "]");
                size--;
                continue;
            }

            Issue rootIssue;
            try {
                // かんばんタスクチケットに紐づく元のチケットを取得
                rootIssue = issueManager.getIssueById(rootId);
            } catch (NotFoundException e) {
                System.out.println("NG format ticket. [" + issue.getId() + " : " + issue.getSubject() + "]");
                size--;
                continue;
            }

            // かんばんタスクチケットがIT確認完了のもので
            if (Story.ITC.isMatch(issue.getParentId())  && Status.DONE.isMatch(issue.getStatusId())) {
                // 元チケットがIT確認完了、完了間近、保留でないものは削除
                if (!RootStatus.DoneITC.isMatch(rootIssue.getStatusId())
                    && !RootStatus.Nearbyclose.isMatch(rootIssue.getStatusId())
                        && !RootStatus.Pending.isMatch(rootIssue.getStatusId())) {
                    System.out.println(rootIssue.getStatusId() + " : " + rootIssue.getStatusName());
                    issueManager.deleteIssue(issue.getId());
                }
                size--;
                continue;
            }

            // 担当者が異なる場合は担当者を変更
            if (!rootIssue.getAssignee().equals(issue.getAssignee())) {
                System.out.println(rootIssue.getAssignee() + "--->" + issue.getAssignee());
                issue.setAssignee(rootIssue.getAssignee());
                issueManager.update(issue);
            }

            // チケット更新
            RootConverterToTask rct;
            for(RootStatus rs : RootStatus.values()) {
                rct = new RootConverterToTask(rs);
                if (rct.isUnMatch(rootId, issue)) {
                    issue.setParentId(rct.getStory().id);
                    issue.setStatusId(rct.getStatus().id);
                    issueManager.update(issue);
                    break;
                }
            }
            size--;
            System.out.println(size);
        }
    }

    private static boolean isInvalidFormat(Integer rootId) {
        return !rootId.toString().matches("[0-9]{4}");
    }

    /**
     * かんばんタスクチケットステータス
     */
    public enum Status {
        STANDBY(1001),
        DOING(1000),
        DONE(9990);

        private int id;

        Status(int i) {
            this.id = i;
        }
        public boolean isMatch(int id) {
            return this.id == id;
        }
    }

    /**
     * かんばんタスクチケットのストーリー
     */
    public enum Story {
        /** Root Cause Analysis */
        RCA(7094),
        /** Programing and Unit Test */
        PGUT(7096),
        /** Modification of Design  */
        MOD(7095),
        /** Request For Handling */
        RFH(7157),
        /** IT Confirming */
        ITC(7141);

        private int id;

        Story(int i) {
            this.id = i;
        }

        public boolean isMatch(int id) {
            return this.id == id;
        }
    }

    /**
     * 元チケットステータス
     */
    public enum RootStatus {
        /** Root Cause Analysis */
        DoneRCA(1063),
        /** Think Guide Line */
        DoneTGL(1066),
        /** Programing and Unit Test */
        DonePGUT(1041),
        /** Modification of Design  */
        DoneMOD(1037),
        /** IT Confirming */
        DoneITC(1045),
        /** Root Cause Analysis */
        DoingRCA(1062),
        /** Think Guide Line */
        DoingTGL(1065),
        /** PG/ UT */
        DoingPGUT(1039),
        /** PG/UT Confirming */
        DoingPGUTC(1040),
        /** Modification of Design  */
        DoingMOD(1036),
        /** IT Confirming */
        DoingITC(1044),
        /** Root Cause Analysis */
        StandbyRCA(1061),
        /** Think Guide Line */
        StandbyTGL(1064),
        /** Request for Handling */
        StandbyRFH(1038),
        /** IT Release Waiting */
        StandbyITRW(1042),
        /** IT Waiting */
        StandbyITW(1043),
        /** Unsatisfactory */
        Unsatisfactory(10017),
        /** Nearbyclose */
        Nearbyclose(10003),
        /** Pending */
        Pending(9980);

        private int id;

        RootStatus(int i) {
            this.id = i;
        }

        public boolean isMatch(int id) {
            return this.id == id;
        }
    }

    /**
     * 元チケットステータスをかんばんタスクチケットのストーリーとステータスに変換する
     */
    public static class RootConverterToTask {

        private Story story;
        private Status status;
        private RootStatus rs;

        public RootConverterToTask(RootStatus rs) {
            this.rs = rs;
            switch (rs) {
                case DoneRCA:
                    this.story = Story.RCA;
                    this.status = Status.DONE;
                    break;
                case DoneTGL:
                    this.story = Story.RCA;
                    this.status = Status.DONE;
                    break;
                case DonePGUT:
                    this.story = Story.PGUT;
                    this.status = Status.DONE;
                    break;
                case DoneMOD:
                    this.story = Story.MOD;
                    this.status = Status.DONE;
                    break;
                case DoneITC:
                    this.story = Story.ITC;
                    this.status = Status.DONE;
                    break;
                case DoingRCA:
                    this.story = Story.RCA;
                    this.status = Status.DOING;
                    break;
                case DoingTGL:
                    this.story = Story.RCA;
                    this.status = Status.DOING;
                    break;
                case DoingPGUT:
                    this.story = Story.PGUT;
                    this.status = Status.DOING;
                    break;
                case DoingPGUTC:
                    this.story = Story.PGUT;
                    this.status = Status.DOING;
                    break;
                case DoingMOD:
                    this.story = Story.MOD;
                    this.status = Status.DOING;
                    break;
                case DoingITC:
                    this.story = Story.ITC;
                    this.status = Status.DOING;
                    break;
                case StandbyRCA:
                    this.story = Story.RCA;
                    this.status = Status.STANDBY;
                    break;
                case StandbyTGL:
                    this.story = Story.RCA;
                    this.status = Status.STANDBY;
                    break;
                case StandbyRFH:
                    this.story = Story.RFH;
                    this.status = Status.STANDBY;
                    break;
                case StandbyITRW:
                    this.story = Story.ITC;
                    this.status = Status.STANDBY;
                    break;
                case StandbyITW:
                    this.story = Story.ITC;
                    this.status = Status.STANDBY;
                    break;
                case Unsatisfactory:
                    this.story = Story.RFH;
                    this.status = Status.STANDBY;
                    break;
                case Nearbyclose:
                    this.story = Story.ITC;
                    this.status = Status.DONE;
                    break;
                default:
                    this.story = null;
                    this.status = null;
                    break;
            }
        }

        public Story getStory() {
            return this.story;
        }

        public Status getStatus() {
            return this.status;
        }

        public boolean isUnMatch(int rootId, Issue issue) {
            return !(this.story == null || this.status == null)
                    && rs.isMatch(rootId)
                    && (!this.story.isMatch(issue.getParentId()) || !this.status.isMatch(issue.getStatusId()));
        }
    }
}
