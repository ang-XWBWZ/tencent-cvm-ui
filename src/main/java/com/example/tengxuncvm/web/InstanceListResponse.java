package com.example.tengxuncvm.web;

import java.util.List;

public class InstanceListResponse {

    private Long totalCount;
    private Integer page;
    private Integer size;
    private List<InstanceDto> items;

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

    public List<InstanceDto> getItems() {
        return items;
    }

    public void setItems(List<InstanceDto> items) {
        this.items = items;
    }
}
