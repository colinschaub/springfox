package springfox.documentation.spring.data.rest;

import javax.servlet.ServletContext;

import static org.springframework.util.StringUtils.isEmpty;

public class Paths {

    public static final String ROOT = "/";

    public static String contextPath(ServletContext context) {
        String path = context.getContextPath();
        return rootPathWhenEmpty(path);
    }

    public static String rootPathWhenEmpty(String path) {
        return !isEmpty(path) ? path : ROOT;
    }

}
