package com.robotshop.shipping.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class CartHelper {

    private static final Logger logger =
            LoggerFactory.getLogger(CartHelper.class);

    private final String baseUrl;
    private final String jwt;

    public CartHelper(String baseUrl, String jwt) {
        this.baseUrl = baseUrl;
        this.jwt     = jwt;
    }

    public String addToCart(String id, String data) {
        StringBuilder buffer = new StringBuilder();
        CloseableHttpClient httpClient = null;

        try {
            String encodedId =
                    URLEncoder.encode(id, StandardCharsets.UTF_8.toString());

            String finalUrl = baseUrl + encodedId;

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);

            httpClient = HttpClients.createDefault();
            HttpPost postRequest = new HttpPost(finalUrl);

            // Forward JWT silently
            postRequest.setHeader("Authorization", jwt);

            StringEntity payload = new StringEntity(data);
            payload.setContentType("application/json");
            postRequest.setEntity(payload);

            CloseableHttpResponse res =
                    httpClient.execute(postRequest);

            int status = res.getStatusLine().getStatusCode();

            if (status == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(res.getEntity().getContent())
                );
                String line;
                while ((line = in.readLine()) != null) {
                    buffer.append(line);
                }
            } else {
                logger.warn(
                    "cart service returned non-200 status: {}",
                    status
                );
            }

            res.close();

        } catch (Exception e) {
            logger.error(
                "failed to call cart service",
                e
            );
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn(
                        "failed to close http client",
                        e
                    );
                }
            }
        }

        return buffer.toString();
    }
}