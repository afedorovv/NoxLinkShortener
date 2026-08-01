package org.nox.shortener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(UrlShortener.class)
class UrlShortenerTest {

    @Autowired
    private UrlShortener shortener;

    @Test
    void shortenGeneratesCodeAndResolvesBack() {
        String longUrl = "https://edsy.org/#/S:0B2wA0B2wA0B2wA0B2wA";
        String code = shortener.shorten(longUrl);
        assertNotNull(code);
        assertTrue(!code.isBlank());
        assertEquals(longUrl, shortener.resolve(code));
    }

    @Test
    void sameUrlReturnsSameCode() {
        String longUrl = "https://edsy.org/#/S:abc";
        assertEquals(shortener.shorten(longUrl), shortener.shorten(longUrl));
    }

    @Test
    void differentUrlsReturnDifferentCodes() {
        assertNotEquals(
                shortener.shorten("https://edsy.org/#/S:aaa"),
                shortener.shorten("https://edsy.org/#/S:bbb"));
    }

    @Test
    void unknownCodeResolvesToNull() {
        assertNull(shortener.resolve("nope"));
    }

    @Test
    void createsFiftyShortLinks() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String longUrl = "https://edsy.org/#/S:testlink" + i;
            String code = shortener.shorten(longUrl);
            assertNotNull(code);
            assertTrue(!code.isBlank());
            assertTrue(codes.add(code), "duplicate code generated: " + code);
            assertEquals(longUrl, shortener.resolve(code));
        }
        assertEquals(50, codes.size());
    }
}
