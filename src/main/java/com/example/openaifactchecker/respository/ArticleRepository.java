package com.example.openaifactchecker.respository;

import com.example.openaifactchecker.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Integer> {

}
