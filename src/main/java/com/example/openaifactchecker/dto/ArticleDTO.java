// src/main/java/com/example/openaifactchecker/dto/ArticleDTO.java

package com.example.openaifactchecker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleDTO {
    private String title;
    private String author;
    private String source;
    private String url;

    // Constructors
    public ArticleDTO() {}

    public ArticleDTO(String title, String author, String source, String url) {
        this.title = title;
        this.author = author;
        this.source = source;
        this.url = url;
    }

    // toString (Optional)
    @Override
    public String toString() {
        return "ArticleDTO{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", source='" + source + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
