package cn.wbombt;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

/**
 * @author George
 */
public class MockDataGenerator {

    private static final int TARGET_ROW_COUNT = 100_0000;
    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        } catch (Exception e) {
            throw new RuntimeException();
        }
        try(SqlSession session = sqlSessionFactory.openSession()){
            List<News> currentNews = session.selectList("cn.wbombt.MockMapper.selectNews");
            int count = TARGET_ROW_COUNT - currentNews.size();
            Random random = new Random();
            try {
                while (count-- > 0){
                    int index = random.nextInt(currentNews.size());
                    News newsTobe = currentNews.get(index);
                    Instant currentTime = newsTobe.getCreatedAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(3600));
                    newsTobe.setModifiedAt(currentTime);
                    newsTobe.setCreatedAt(currentTime);
                    session.insert("cn.wbombt.MockMapper.insertNews", newsTobe);
                    System.out.println(count);
                }
                session.commit();
            }catch (Exception e){
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
