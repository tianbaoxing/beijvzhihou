package com.beijvzhihou.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private List<T> list;
    private long total;   // 总记录数
    private int page;    // 当前页
    private int size;    // 每页大小
    private int pages;   // 总页数 = (total + size - 1) / size

    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        int pages = (int) ((total + size - 1) / size);
        return new PageResult<>(list, total, page, size, pages);
    }
}
