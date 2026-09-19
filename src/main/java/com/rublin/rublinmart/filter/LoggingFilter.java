package com.rublin.rublinmart.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();
        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        chain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("HTTP {} {} -> Status {} ({} ms)", method, uri, httpResponse.getStatus(), duration);
    }

    @Override
    public void destroy() {}
}
