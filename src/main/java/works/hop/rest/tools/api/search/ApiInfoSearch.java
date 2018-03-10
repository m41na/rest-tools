package works.hop.rest.tools.api.search;

import java.util.List;

import works.hop.rest.tools.api.ApiReq;

public class ApiInfoSearch {

    private final SearchStrategy strategy;

    public ApiInfoSearch() {
        this.strategy = new PlainPathSearch();
        SearchStrategy tokenPathStrategy = new TokenPathSearch();
        this.strategy.setNextStrategy(tokenPathStrategy);
    }

    public ApiReq searchEndpoint(List<ApiReq> list, String path, String method) {
        return strategy.searchEndpoint(list, path, method);
    }
}
