package com.beijvzhihou.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Nested
    @DisplayName("Result.ok 测试")
    class OkTest {

        @Test
        @DisplayName("ok() 返回成功结果，无数据")
        void ok_noData_returnsSuccessWithNull() {
            Result<Void> result = Result.ok();

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).isNull();
        }

        @Test
        @DisplayName("ok(data) 返回成功结果，带数据")
        void ok_withData_returnsSuccessWithData() {
            Result<String> result = Result.ok("hello");

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Result.fail 测试")
    class FailTest {

        @Test
        @DisplayName("fail(message) 返回失败结果")
        void fail_withMessage_returnsFailResult() {
            Result<Void> result = Result.fail("出错了");

            assertThat(result.getCode()).isNotEqualTo(200);
            assertThat(result.getMessage()).isEqualTo("出错了");
            assertThat(result.getData()).isNull();
        }

        @Test
        @DisplayName("fail(code, message) 返回指定错误码的失败结果")
        void fail_withCodeAndMessage_returnsFailResult() {
            Result<Void> result = Result.fail(403, "禁止访问");

            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).isEqualTo("禁止访问");
        }
    }
}
