package cn.wbombt;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author George
 */
public class MyBatisCrawlerDao implements CrawlerDao {
    private final SqlSessionFactory sqlSessionFactory;

    private final String MAPPER_NAMESPACE = "cn.wbombt.MyMapper";

    public MyBatisCrawlerDao() {
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNextLinkThenDelete() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String nextLink = session.selectOne(MAPPER_NAMESPACE + ".selectNextAvailable");
            if (nextLink != null) {
                session.delete(MAPPER_NAMESPACE + ".deleteLink", nextLink);
            }
            return nextLink;
        }
    }

    @Override
    public void insertProcessedLink(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert(MAPPER_NAMESPACE + ".insertProcessedLink", link);
        }
    }

    @Override
    public void insertLinkToBeProcessedLink(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert(MAPPER_NAMESPACE + ".insertLinkToBeProcessedLink", link);
        }
    }

    @Override
    public void insertNewsIntoData(String url, String title, String content) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            News news = new News();
            news.setUrl(url);
            news.setTitle(title);
            news.setContent(content);
            session.insert(MAPPER_NAMESPACE + ".insertNews", news);
        }
    }

    @Override
    public boolean isLinkProcessed(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Integer count = session.selectOne(MAPPER_NAMESPACE + ".countLink", link);
            return count != null && count > 0;
        }
    }
}
