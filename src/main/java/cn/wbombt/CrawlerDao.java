package cn.wbombt;

import java.sql.SQLException;

/**
 * @author George
 */
public interface CrawlerDao {
    String getNextLinkThenDelete() throws SQLException;

    void insertProcessedLink(String link);

    void insertLinkToBeProcessedLink(String link);

    void insertNewsIntoData(String url, String title, String content);

    boolean isLinkProcessed(String link);
}
