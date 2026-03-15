package com.example.tengxuncvm.web;

import java.util.List;

public class BillListResponse {

    private Long totalCount;
    private Integer page;
    private Integer size;
    private List<BillDto> items;

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public List<BillDto> getItems() {
        return items;
    }

    public void setItems(List<BillDto> items) {
        this.items = items;
    }
}