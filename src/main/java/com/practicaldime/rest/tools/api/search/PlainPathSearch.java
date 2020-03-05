package com.practicaldime.rest.tools.api.search;

import com.practicaldime.common.entity.rest.ApiReq;
import com.practicaldime.rest.tools.api.ApiReqComparator;

import java.util.Collections;
import java.util.List;

public class PlainPathSearch implements SearchStrategy {

    private SearchStrategy next;

    @Override
    public void setNextStrategy(SearchStrategy strategy) {
        this.next = strategy;
    }

    @Override
    public ApiReq searchEndpoint(List<ApiReq> list, String path, String method) {
        ApiReq criteria = new ApiReq();
        criteria.setPath(path);
        criteria.setMethod(method);
        //create comparator and search
        ApiReqComparator comparator = new ApiReqComparator();
        Collections.sort(list, comparator);
        int index = Collections.binarySearch(list, criteria, comparator);
        return (index > -1) ? list.get(index) : (next != null) ? next.searchEndpoint(list, path, method) : null;
    }
}
