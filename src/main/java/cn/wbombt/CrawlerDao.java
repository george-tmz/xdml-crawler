package cn.wbombt;

import java.sql.SQLException;

/**
 * @author George
 */
public interface CrawlerDao {
    String getNextLink() throws SQLException;

    String getNextLinkThenDelete() throws SQLException;

    void updateData(String link, String sql);

    void insertNewsIntoData(String url, String title, String content);

    boolean isLinkProcessed(String link);
}
