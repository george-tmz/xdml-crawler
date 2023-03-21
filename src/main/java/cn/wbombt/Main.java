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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author George
 */
public class Main {
    public static void main(String[] args) {
        // 待处理的链接池
        List<String> linkPool = new ArrayList<>();
        linkPool.add("https://sina.cn/");
//        已处理过的连接池
        Set<String> processedLinks = new HashSet<>();

        while (!linkPool.isEmpty()) {
            //remove()函数返回被删除的元素
            String link = linkPool.remove(linkPool.size() - 1);
            //链接正确性检查
            if (link.startsWith("//")) {
                link = "https:" + link;
            }
//          是否处理过
            if (processedLinks.contains(link)) {
                continue;
            }
            // 是要处理的
            if (link.contains("news.sina.cn") && !link.contains("passport.sina.cn") || "https://sina.cn/".equals(link)) {
                try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                    ClassicHttpRequest httpGet = ClassicRequestBuilder.get(link)
                            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36")
                            .build();
                    String finalLink = link;
                    httpclient.execute(httpGet, response -> {
                        System.out.println(finalLink);
                        System.out.println(response.getReasonPhrase());
                        final HttpEntity entity1 = response.getEntity();
                        String html = EntityUtils.toString(entity1);
                        Document doc = Jsoup.parse(html);
                        // 获取抓取页面的链接
                        ArrayList<Element> links = doc.select("a");
                        for (Element aTag : links) {
                            linkPool.add(aTag.attr("href"));
                        }
                        //假如这是一个新闻页面，就存入数据库，否则，就什么都不做
                        ArrayList<Element> articleTags = doc.select("article");
                        if (!articleTags.isEmpty()) {
//                            for (Element articleTag : articleTags) {
                            System.out.println(articleTags.get(0).child(0).text());
//                            }
                        }
                        processedLinks.add(finalLink);
                        return null;
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }
}
