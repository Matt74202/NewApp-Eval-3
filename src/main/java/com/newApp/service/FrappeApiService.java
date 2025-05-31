package com.newApp.service;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class FrappeApiService {

    private final RestTemplate restTemplate;
    private final CookieStore cookieStore;
    private final String baseUrl;
    private final HttpSession session;

    @Autowired
    public FrappeApiService(RestTemplate restTemplate, CookieStore cookieStore, 
                           @Value("${frappe.api.url}") String baseUrl, HttpSession session) {
        this.restTemplate = restTemplate;
        this.cookieStore = cookieStore;
        this.baseUrl = baseUrl;
        this.session = session;
    }

    public String validateUserCredentials(String username, String password) {
        try {
            System.out.println("Attempting to validate credentials for username: " + username);
            String endpoint = "method/login";
            String url = baseUrl + "/api/" + endpoint;
            System.out.println("Constructed URL: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            String requestBody = "usr=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                               "&pwd=" + URLEncoder.encode(password, StandardCharsets.UTF_8);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            cookieStore.clear();
            System.out.println("Cleared cookies before login request.");

            System.out.println("Sending login request to: " + url);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            List<Cookie> cookies = cookieStore.getCookies();
            System.out.println("Cookies received after login:");
            for (Cookie cookie : cookies) {
                System.out.println("Cookie: " + cookie.getName() + "=" + cookie.getValue() + 
                                 "; Domain=" + cookie.getDomain() + "; Path=" + cookie.getPath());
                if (cookie.getName().equals("sid")) {
                    session.setAttribute("frappeSid", cookie.getValue());
                }
            }

            String rawResponse = response.getBody() != null ? response.getBody().toString() : "null";
            System.out.println("Raw response from /api/method/login: " + rawResponse);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("full_name") && cookies.stream().anyMatch(c -> c.getName().equals("sid"))) {
                    System.out.println("Login successful with HTTP status: " + response.getStatusCode());
                    return null;
                } else {
                    System.out.println("Login failed: No 'full_name' or 'sid' cookie in response.");
                    return "Login failed: Invalid response or no session cookie.";
                }
            } else {
                System.out.println("Login failed: Invalid response. HTTP status: " + response.getStatusCode());
                return "Login failed: Invalid response. HTTP status: " + response.getStatusCode();
            }
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error during login: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return "HTTP Error during login: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        } catch (RestClientException e) {
            System.err.println("Failed to connect to ERPNext for login: " + e.getMessage());
            return "Failed to connect to ERPNext for login: " + e.getMessage();
        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            return "Unexpected error during login: " + e.getMessage();
        }
    }

    public boolean isSessionValid() {
        try {
            String sid = (String) session.getAttribute("frappeSid");
            if (sid == null) {
                System.out.println("No session cookie found.");
                return false;
            }
            String url = baseUrl + "/api/method/frappe.auth.get_logged_user";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", "sid=" + sid);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Session validation failed: " + e.getMessage());
            return false;
        }
    }

    public <T> T getResource(String resource, String filters, Class<T> responseType) {
        try {
            String sid = (String) session.getAttribute("frappeSid");
            if (sid == null || !isSessionValid()) {
                throw new RuntimeException("Session non authentifiée ou expirée");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", "sid=" + sid);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + "/api/resource/" + resource + (filters != null ? "?filters=" + filters : "");
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new RuntimeException("Session expirée, veuillez vous reconnecter");
            }
            throw e;
        }
    }

    public <T> T postResource(String resource, Object body, Class<T> responseType) {
        try {
            String sid = (String) session.getAttribute("frappeSid");
            if (sid == null || !isSessionValid()) {
                throw new RuntimeException("Session non authentifiée ou expirée");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", "sid=" + sid);
            headers.set("Content-Type", "application/json");
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            String url = baseUrl + "/api/resource/" + resource;
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new RuntimeException("Session expirée, veuillez vous reconnecter");
            }
            throw e;
        }
    }
}