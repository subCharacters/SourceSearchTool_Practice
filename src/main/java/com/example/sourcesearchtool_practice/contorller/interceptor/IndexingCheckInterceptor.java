package com.example.sourcesearchtool_practice.contorller.interceptor;

import com.example.sourcesearchtool_practice.util.IndexingCheck;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class IndexingCheckInterceptor implements HandlerInterceptor {
    private final IndexingCheck indexingCheck;

    public IndexingCheckInterceptor(IndexingCheck indexingCheck) {
        this.indexingCheck = indexingCheck;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (indexingCheck.isIndexing()) {
            response.sendRedirect("/indexingProgress");
            return false;
        }

//        if (uri.equals("/") || uri.equals("/search") || uri.equals("/search/")) {
//            response.sendRedirect("/top");
//        }

        return true;

    }
}
