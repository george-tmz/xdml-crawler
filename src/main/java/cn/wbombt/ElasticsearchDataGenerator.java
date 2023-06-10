package cn.wbombt;


import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.xcontent.XContentType;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author George
 */
public class ElasticsearchDataGenerator {
    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        } catch (Exception e) {
            throw new RuntimeException();
        }
        List<News> newsFromMySql = getNewsFromMySql(sqlSessionFactory);
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            for (News news : newsFromMySql) {
                IndexRequest request = new IndexRequest("news");
                Map<String, String> data = new HashMap<>();
                data.put("url", news.getUrl());
                data.put("title", news.getTitle());
                data.put("content", news.getContent());
                data.put("createdAt", news.getCreatedAt().toString());
                data.put("modifiedAt", news.getModifiedAt().toString());
                request.source(data, XContentType.JSON);
                IndexResponse response = client.index(request, RequestOptions.DEFAULT);
                System.out.println(response.status().getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static List<News> getNewsFromMySql(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("cn.wbombt.MockMapper.selectNews");
        }
    }
}
