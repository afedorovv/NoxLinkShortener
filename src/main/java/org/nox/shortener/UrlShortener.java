package org.nox.shortener;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UrlShortener {

    private static final String ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final long BASE = ALPHABET.length();

    private final ShortLinkRepository repository;
    private final AtomicLong counter;

    public UrlShortener(ShortLinkRepository repository) {
        this.repository = repository;
        this.counter = new AtomicLong(0);
    }

    @PostConstruct
    void initCounter() {
        counter.set(repository.count());
    }

    public String shorten(String url) {
        Optional<ShortLink> existing = repository.findByUrl(url);
        if (existing.isPresent()) {
            return existing.get().getCode();
        }
        String code = encode(counter.getAndIncrement());
        repository.save(new ShortLink(code, url));
        return code;
    }

    public String resolve(String code) {
        return repository.findByCode(code)
                .map(ShortLink::getUrl)
                .orElse(null);
    }

    private String encode(long num) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(ALPHABET.charAt((int) (num % BASE)));
            num /= BASE;
        } while (num > 0);
        return sb.reverse().toString();
    }
}
