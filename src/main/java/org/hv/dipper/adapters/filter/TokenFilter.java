package org.hv.dipper.adapters.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.hv.biscuits.config.FilterPathConfig;
import org.hv.biscuits.controller.Body;
import org.hv.biscuits.utils.PathMatcher;
import org.hv.dipper.config.TokenConfig;
import org.hv.dipper.domain.aggregation.UserView;
import org.hv.dipper.domain.port.in.AuthorityCheck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author wujianchuan
 */
@Component
@Order(Integer.MIN_VALUE + 1)
public class TokenFilter implements Filter {
    private final static String OPTIONS = "OPTIONS";
    @Value("${spring.application.name}")
    private String serverId;
    private final FilterPathConfig filterPathConfig;
    private final TokenConfig tokenConfig;
    private final Set<String> excludeUrlPatterns = new LinkedHashSet<>();

    private final AuthorityCheck authorityCheck;

    public TokenFilter(FilterPathConfig filterPathConfig, TokenConfig tokenConfig, AuthorityCheck authorityCheck) {
        this.filterPathConfig = filterPathConfig;
        this.tokenConfig = tokenConfig;
        this.authorityCheck = authorityCheck;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String excludeUrlStr = this.filterPathConfig.getExcludeUrlPatterns();
        if (excludeUrlStr != null && excludeUrlStr.length() > 0) {
            this.excludeUrlPatterns.addAll(Arrays.asList(this.filterPathConfig.getExcludeUrlPatterns().replaceAll(" ", "").split(",")));
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getServletPath();
        if (path == null) {
            path = request.getPathInfo();
        }
        boolean filterTurnOn = this.filterPathConfig.getTurnOn() == null || this.filterPathConfig.getTurnOn();
        if (!filterTurnOn || matchExclude(path)) {
            freeRequest(req, res, chain, request, response);
            return;
        }
        String[] splitPath = path.split("/");
        String bundleId = splitPath[splitPath.length - 2];
        String actionId = splitPath[splitPath.length - 1];
        if (OPTIONS.equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(req, res);
        }
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith(this.tokenConfig.getTokenHead())) {
            if (authorityCheck.freeAction(serverId, bundleId, actionId)) {
                freeRequest(req, res, chain, request, response);
                return;
            }
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            Body responseBody = Body.warning().reLogin().message("Missing or invalid Authorization header");
            this.refuse(response, responseBody);
            return;
        }
        Map<String, Object> checkResult = this.authorityCheck.check(this.serverId, bundleId, actionId, token.replace(tokenConfig.getTokenHead(), ""));
        Boolean expired = (Boolean) checkResult.get("expired");
        if (expired) {
            this.refuse(response, Body.warning().reLogin().message((String) checkResult.get("errorMessage")));
            return;
        }
        Boolean allowed = (Boolean) checkResult.get("allowed");
        if (!allowed) {
            this.refuse(response, Body.warning().message((String) checkResult.get("errorMessage")));
            return;
        }
        req.setAttribute("avatar", checkResult.get("avatar"));
        req.setAttribute("userName", checkResult.get("avatar"));
        req.setAttribute("departmentUuid", checkResult.get("departmentUuid"));
        req.setAttribute("departmentName", checkResult.get("departmentName"));
        req.setAttribute("businessDepartmentUuid", checkResult.get("businessDepartmentUuid"));
        req.setAttribute("businessDepartmentName", checkResult.get("businessDepartmentName"));
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        this.excludeUrlPatterns.clear();
    }

    private boolean matchExclude(String path) {
        PathMatcher pathMatcher = new PathMatcher();
        for (String excludeUrlPattern : this.excludeUrlPatterns) {
            if (pathMatcher.matching(excludeUrlPattern, path)) {
                return true;
            }
        }
        return false;
    }

    private void freeRequest(ServletRequest req, ServletResponse res, FilterChain chain, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        String tokenHead = this.tokenConfig.getTokenHead();
        if (token != null && token.startsWith(tokenHead)) {
            UserView userView = this.authorityCheck.parseUserView(token.replace(tokenHead, ""));
            if (userView != null) {
                request.setAttribute("avatar", userView.getAvatar());
                request.setAttribute("userName", userView.getName());
                request.setAttribute("departmentUuid", userView.getDepartmentUuid());
                request.setAttribute("departmentName", userView.getDepartmentName());
                request.setAttribute("businessDepartmentUuid", userView.getBusinessDepartmentUuid());
                request.setAttribute("businessDepartmentName", userView.getBusinessDepartmentName());
            }
        }
        chain.doFilter(req, res);
    }

    private void refuse(HttpServletResponse response, Body responseBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setStatus(org.springframework.http.HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}
