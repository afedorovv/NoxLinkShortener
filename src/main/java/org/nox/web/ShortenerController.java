package org.nox.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nox.shortener.UrlShortener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
public class ShortenerController {

    private final UrlShortener urlShortener;

    public ShortenerController(UrlShortener urlShortener) {
        this.urlShortener = urlShortener;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shorten(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "url is required"));
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return ResponseEntity.badRequest().body(Map.of("error", "url must be a valid http(s) link"));
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "url is not a valid URI"));
        }
        if (!"edsy.org".equalsIgnoreCase(uri.getHost())) {
            return ResponseEntity.badRequest().body(Map.of("error", "url host must be edsy.org"));
        }
        String code = urlShortener.shorten(url);
        String shortUrl = baseUrl(request) + "/s/" + code;
        return ResponseEntity.ok(Map.of("shortUrl", shortUrl, "code", code, "original", url));
    }

    @GetMapping("/s/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String url = urlShortener.resolve(code);
        if (url == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }

    private String baseUrl(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-Proto");
        String scheme = forwarded != null ? forwarded : request.getScheme();
        if (!scheme.startsWith("https")) {
            scheme = "https";
        }
        return scheme + "://" + request.getServerName();
    }
}
