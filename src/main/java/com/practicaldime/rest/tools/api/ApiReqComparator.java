package com.practicaldime.rest.tools.api;

import java.util.Comparator;

import com.practicaldime.common.entity.rest.ApiReq;

/**
 * Search for item. Best match is when paths match, otherwise use path and
 * method combo.
 *
 */
public class ApiReqComparator implements Comparator<ApiReq> {

    @Override
    public int compare(ApiReq o1, ApiReq o2) {
        final int EQUAL = 0;

        if (o1 == o2) {
            return EQUAL;
        }

        int comparison = o1.getPath().compareTo(o2.getPath());
        if (comparison != EQUAL) {
            return comparison;
        }

        comparison = o1.getMethod().toLowerCase().compareTo(o2.getMethod().toLowerCase());
        if (comparison != EQUAL) {
            return comparison;
        }

        return EQUAL;
    }
}
