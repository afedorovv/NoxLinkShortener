package org.nox.web;

import org.junit.jupiter.api.Test;
import org.nox.shortener.ShortLink;
import org.nox.shortener.ShortLinkRepository;
import org.nox.shortener.UrlShortener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortenerController.class)
@Import(UrlShortener.class)
class ShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortLinkRepository repository;

    @Test
    void shortenReturnsShortUrl() throws Exception {
        when(repository.findByUrl(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(ShortLink.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://edsy.org/#/S:abc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value(matchesPattern("https://localhost/s/\\w+")))
                .andExpect(jsonPath("$.original").value("https://edsy.org/#/S:abc"));
    }

    @Test
    void shortenReusesExistingCode() throws Exception {
        when(repository.findByUrl("https://edsy.org/#/S:abc"))
                .thenReturn(Optional.of(new ShortLink("existing", "https://edsy.org/#/S:abc")));

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://edsy.org/#/S:abc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("existing"));
    }

    @Test
    void shortenRejectsMissingUrl() throws Exception {
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortenRejectsNonHttpUrl() throws Exception {
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"ftp://edsy.org/x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redirectFollowsShortUrl() throws Exception {
        when(repository.findByCode("abc123"))
                .thenReturn(Optional.of(new ShortLink("abc123", "https://edsy.org/#/S:longvalue")));

        mockMvc.perform(get("/s/abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "https://edsy.org/#/S:longvalue"));
    }

    @Test
    void unknownCodeReturns404() throws Exception {
        when(repository.findByCode("doesnotexist")).thenReturn(Optional.empty());

        mockMvc.perform(get("/s/doesnotexist"))
                .andExpect(status().isNotFound());
    }
}
