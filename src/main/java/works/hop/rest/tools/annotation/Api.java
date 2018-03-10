package works.hop.rest.tools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Api {

    String id();

    String name() default "replace with target method's name";

    String descr() default "description currently not available";

    String method() default "GET";

    String path() default "/";

    String query() default "";

    String[] consumes() default {};

    String[] produces() default {"application/json"};

    String[] headers() default {};

    String entity() default "";

    String response() default "response currently not available";

    Class<?> onsuccess() default Boolean.class;

    String onerror() default "";

    int status() default 200;

    String url() default "http:\\\\localhost:8081\\simple-tools\\rws";
}
