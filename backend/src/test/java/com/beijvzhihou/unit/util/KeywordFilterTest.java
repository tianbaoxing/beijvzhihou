package com.beijvzhihou.unit.util;

import com.beijvzhihou.util.KeywordFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordFilterTest {

    private KeywordFilter keywordFilter;

    @BeforeEach
    void setUp() {
        keywordFilter = new KeywordFilter();
        keywordFilter.init();
    }

    @Nested
    @DisplayName("关键词检测测试")
    class ContainsKeywordTest {

        @Test
        @DisplayName("null 内容返回 false")
        void containsKeyword_null_returnsFalse() {
            assertThat(keywordFilter.containsKeyword(null)).isFalse();
        }

        @Test
        @DisplayName("空字符串返回 false")
        void containsKeyword_empty_returnsFalse() {
            assertThat(keywordFilter.containsKeyword("")).isFalse();
        }

        @Test
        @DisplayName("安全内容返回 false")
        void containsKeyword_safeContent_returnsFalse() {
            assertThat(keywordFilter.containsKeyword("今天面试被拒了，好难过")).isFalse();
        }
    }

    @Nested
    @DisplayName("查找关键词测试")
    class FindFirstKeywordTest {

        @Test
        @DisplayName("null 内容返回 null")
        void findFirstKeyword_null_returnsNull() {
            assertThat(keywordFilter.findFirstKeyword(null)).isNull();
        }

        @Test
        @DisplayName("空字符串返回 null")
        void findFirstKeyword_empty_returnsNull() {
            assertThat(keywordFilter.findFirstKeyword("")).isNull();
        }

        @Test
        @DisplayName("安全内容返回 null")
        void findFirstKeyword_safeContent_returnsNull() {
            assertThat(keywordFilter.findFirstKeyword("今天面试被拒了，好难过")).isNull();
        }
    }
}
