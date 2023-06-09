package cn.wbombt;

import java.io.File;
import java.sql.*;

/**
 * @author George
 */
public class JdbcCrawlerDao implements CrawlerDao {
    private final Connection connection;

    public JdbcCrawlerDao() {
        try {
            File projectDir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
            String jdbcUrl = "jdbc:h2:file:" + new File(projectDir, "news").getAbsolutePath();
            connection = DriverManager.getConnection(jdbcUrl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNextLink() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT link from LINKS_TO_BE_PROCESSED LIMIT 1")) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        String link = getNextLink();
        if (link != null) {
            updateData(link, "DELETE FROM LINKS_TO_BE_PROCESSED WHERE link = ?");
        }
        return link;
    }

    private void updateData(String link, String sql) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertProcessedLink(String link) {

    }

    @Override
    public void insertLinkToBeProcessedLink(String link) {

    }

    @Override
    public void insertNewsIntoData(String url, String title, String content) {
        try (PreparedStatement statement = connection.prepareStatement("insert into news (title, content, url, created_at, modified_at) values (?,?,?,NOW(),NOW())")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, url);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean isLinkProcessed(String link) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT link FROM LINKS_ALREADY_PROCESSED WHERE link = ?")) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
