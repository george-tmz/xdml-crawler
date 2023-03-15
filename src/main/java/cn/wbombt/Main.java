package cn.wbombt;


import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            ClassicHttpRequest httpGet = ClassicRequestBuilder.get("https://sina.cn/")
                    .build();
            httpclient.execute(httpGet, response -> {
                final HttpEntity entity1 = response.getEntity();
                System.out.println(EntityUtils.toString(entity1));
                return null;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
