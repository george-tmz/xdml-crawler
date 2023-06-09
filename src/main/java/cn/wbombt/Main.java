package cn.wbombt;


import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * @author George
 */
public class Main {
    public static void main(String[] args) throws SQLException {
        //链接数据库
        File projectDir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
        String jdbcUrl = "jdbc:h2:file:" + new File(projectDir, "news").getAbsolutePath();
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            String link;
            while ((link = getNextLinkThenDelete(connection)) != null) {
                if (isLinkProcessed(connection, link)) {
                    continue;
                }
                if (isInterestingPage(link)) {
                    Document doc = httpGetAndParseHtml(link);
                    parseUrlsFromPageAndStoreIntoDatabase(connection, doc);
                    storeIntoDatabaseIfItIsNewsPage(doc);
                    updateData(connection, link, "insert into LINKS_ALREADY_PROCESSED (LINK) values (?)");
                }
            }
        }
    }

    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) {
        doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(href -> {
            if (!href.isEmpty() && !href.toLowerCase().startsWith("javascript") && !href.startsWith("#")) {
                if (href.startsWith("//")) {
                    href = "https:" + href;
                }
                updateData(connection, href, "insert into LINKS_TO_BE_PROCESSED (LINK) values (?)");
            }
        });
    }

    private static String getNextLink(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT link from LINKS_TO_BE_PROCESSED LIMIT 1")) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String link = getNextLink(connection);
        if (link != null) {
            updateData(connection, link, "DELETE FROM LINKS_TO_BE_PROCESSED WHERE link = ?");
        }
        return link;
    }

    private static void storeIntoDatabaseIfItIsNewsPage(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            System.out.println(articleTags.get(0).child(0).text());
        }
    }

    private static Document httpGetAndParseHtml(String link) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            ClassicHttpRequest httpGet = ClassicRequestBuilder.get(link).addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36").build();
            return httpclient.execute(httpGet, response -> {
                final HttpEntity entity = response.getEntity();
                String html = EntityUtils.toString(entity);
                return Jsoup.parse(html);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isInterestingPage(String link) {
        return (isNewsPage(link) && isNotLoginPage(link)) || isHomePage(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static boolean isHomePage(String link) {
        return "https://sina.cn/".equals(link);
    }

    private static void updateData(Connection connection, String link, String sql) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT link FROM LINKS_ALREADY_PROCESSED WHERE link = ?")) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
