package com.beijvzhihou.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 关键词过滤工具：检测违禁词、敏感词
 * MVP 阶段用内存正则匹配，关键词列表从 resources/keywords.txt 加载
 */
@Slf4j
@Component
public class KeywordFilter {

    private final List<Pattern> patterns = new ArrayList<>();

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("keywords.txt");
            if (resource.exists()) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            // 转义正则特殊字符
                            String escaped = Pattern.quote(line);
                            patterns.add(Pattern.compile(escaped, Pattern.CASE_INSENSITIVE));
                        }
                    }
                }
            }
            log.info("关键词过滤加载了 {} 个词条", patterns.size());
        } catch (Exception e) {
            log.warn("关键词文件加载失败，使用空列表: {}", e.getMessage());
        }
    }

    /**
     * 检测内容是否包含违禁词
     * @param content 待检测内容
     * @return true=包含违禁词，false=安全
     */
    public boolean containsKeyword(String content) {
        if (content == null || content.isEmpty()) return false;
        for (Pattern pattern : patterns) {
            if (pattern.matcher(content).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取第一个匹配的违禁词（用于提示）
     */
    public String findFirstKeyword(String content) {
        if (content == null || content.isEmpty()) return null;
        for (Pattern pattern : patterns) {
            java.util.regex.Matcher m = pattern.matcher(content);
            if (m.find()) {
                return m.group();
            }
        }
        return null;
    }
}
