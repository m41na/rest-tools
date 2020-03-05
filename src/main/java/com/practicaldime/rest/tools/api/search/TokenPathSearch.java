package com.practicaldime.rest.tools.api.search;

import java.util.Iterator;
import java.util.List;

import com.practicaldime.common.entity.rest.ApiReq;

public class TokenPathSearch implements SearchStrategy {

    private SearchStrategy next;

    @Override
    public void setNextStrategy(SearchStrategy strategy) {
        this.next = strategy;
    }

    @Override
    public ApiReq searchEndpoint(List<ApiReq> list, String path, String method) {
        String[] tokens = path.split("/"); // example /user/2/todo/1 -> [user, 2, todo, 1]
        ApiReq matchedEndpoint = null;
        for (Iterator<ApiReq> iter = list.iterator(); iter.hasNext();) {
            ApiReq info = iter.next();
            String[] infoTokens = info.getPath().split("/"); //example /user/:userId/todo/:todoId -> [user, :userId, todo, :todoId]
            if (infoTokens.length != tokens.length) {
                iter.remove();
                continue;
            }
            for (int i = 0; i < infoTokens.length; i++) {
                if (!infoTokens[i].equals(tokens[i])) {
                    if (!infoTokens[i].startsWith(":")) {
                        iter.remove();
                        break;
                    }
                }
            }

            if (!info.getMethod().equalsIgnoreCase(method)) {
                iter.remove();
                continue;
            }

            info.setPath(path);
            matchedEndpoint = info;
            break;
        }
        return (matchedEndpoint != null) ? matchedEndpoint : (next != null) ? next.searchEndpoint(list, path, method) : null;
    }
}
