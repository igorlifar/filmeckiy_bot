package com.filmeckiy.bot;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.InetSocketAddress;

/**
 * @author egor
 */
public class TestTor {
    private static final Logger logger = LogManager.getLogger(TestTor.class);

    public static void main(String[] args)throws Exception {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new TorConnectionSocketFactory())
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);

        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();

        try {
            InetSocketAddress socksaddr = new InetSocketAddress("localhost", 9050);
            HttpClientContext context = HttpClientContext.create();
            context.setAttribute("socks.address", socksaddr);

            HttpHost target = new HttpHost("kinopoisk.ru", 80, "http");
            HttpGet request = new HttpGet("/");

            request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.setHeader(
                    "User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/600.4.10 " +
                            "(KHTML, like Gecko) Version/8.0.4 Safari/600.4.10");

            System.out.println("Executing request " + request + " to " + target + " via SOCKS proxy " + socksaddr);
            CloseableHttpResponse response = httpclient.execute(target, request, context);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());

                StringWriter stringWriter = new StringWriter();
                IOUtils.copy(new InputStreamReader(response.getEntity().getContent(), "windows-1251"), stringWriter);

                System.out.println(stringWriter.toString());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

}
