package works.hop.rest.tools.api;

public interface RequestHandler<T> {

    T handle(ApiReq endpoint) throws Exception;
}
