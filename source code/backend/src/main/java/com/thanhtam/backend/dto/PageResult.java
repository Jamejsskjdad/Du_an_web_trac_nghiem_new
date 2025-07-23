package com.thanhtam.backend.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.thanhtam.backend.dto.pagination.PaginationDetails;

import lombok.Data;

@Data
public class PageResult<T> {
    private List<T> data;
    private PaginationDetails paginationDetails;

    // Constructor cho Page<T>
    public PageResult(Page<T> page) {
        this.data = page.getContent();
        this.paginationDetails = new PaginationDetails(page);
    }

    // Constructor cho List<T> + PaginationDetails (tùy trường hợp bạn muốn tự set)
    public PageResult(List<T> data, PaginationDetails paginationDetails) {
        this.data = data;
        this.paginationDetails = paginationDetails;
    }
}
