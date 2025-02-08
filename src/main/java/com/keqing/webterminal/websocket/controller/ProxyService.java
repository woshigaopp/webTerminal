package com.keqing.webterminal.websocket.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.util.Enumeration;

/**
 * @author keqing
 */
@Service
public class ProxyService {
    // The address of the target server
    @Value("${proxy.address}")
    private String address;

    public void proxy(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String body, HttpMethod httpMethod) throws Exception {
        // Initialize the RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Add headers
        HttpHeaders headers = new HttpHeaders();
        //copy headers from the original request to the proxy request, you can define which headers you want to copy
        HttpHeaders requestHeaders = new HttpHeaders();
        for (Enumeration<String> headerNames = httpServletRequest.getHeaderNames(); headerNames.hasMoreElements(); ) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, httpServletRequest.getHeader(headerName));
        }

        // Build the HttpEntity object
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // Prepare request callback
        RequestCallback requestCallback = restTemplate.httpEntityCallback(entity);

        // Prepare response extractor
        ResponseExtractor<Void> responseExtractor = clientHttpResponse -> {
            // Set status code and headers
            httpServletResponse.setStatus(clientHttpResponse.getStatusCode().value());
            clientHttpResponse.getHeaders().forEach((name, values) -> values.forEach(value -> httpServletResponse.addHeader(name, value)));

            // Copy body
            ServletOutputStream out = httpServletResponse.getOutputStream();
            byte[] buf = new byte[4096]; // choose a good value here
            int bytesRead;
            while ((bytesRead = clientHttpResponse.getBody().read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }

            return null;
        };

        // Execute the request
        restTemplate.execute(requestUrl(httpServletRequest.getRequestURI()), httpMethod, requestCallback, responseExtractor);

    }

    private String requestUrl(String requestUri){
        requestUri = requestUri.replaceFirst("proxy", "");
        return this.address + requestUri;
    }
}