package com.nexusmart.api.dto.request;

public class SearchRequest {
    private String query;
    private Integer limit;

    public SearchRequest() {}

    public SearchRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
