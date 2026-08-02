package org.nox.shortener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "short_links")
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    protected ShortLink() {
    }

    public ShortLink(String code, String url) {
        this.code = code;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getUrl() {
        return url;
    }
}
