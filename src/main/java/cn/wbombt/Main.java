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
        //已处理过的连接池
        Set<String> processedLinks = new HashSet<>();
        linkPool.add("https://sina.cn/");
        while (!linkPool.isEmpty()) {
            //remove()函数返回被删除的元素
            String link = linkPool.remove(linkPool.size() - 1);
            //链接正确性检查
            if (link.startsWith("//")) {
                link = "https:" + link;
            }
            if (processedLinks.contains(link)) {
                continue;
            }
            if (isInterestingPage(link)) {
                Document doc = httpGetAndParseHtml(link);
                // 获取页面的链接
                doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);
                storeIntoDatabaseIfItIsNewsPage(doc);
                processedLinks.add(link);
            }
        }

    }

    private static void storeIntoDatabaseIfItIsNewsPage(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            System.out.println(articleTags.get(0).child(0).text());
        }
    }

    private static Document httpGetAndParseHtml(String link) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            ClassicHttpRequest httpGet = ClassicRequestBuilder.get(link)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36")
                    .build();
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
}
