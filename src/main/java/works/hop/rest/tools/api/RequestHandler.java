package works.hop.rest.tools.api;

import works.hop.rest.tools.model.ApiReq;

public interface RequestHandler<T> {

    T handle(ApiReq endpoint) throws Exception;
}
