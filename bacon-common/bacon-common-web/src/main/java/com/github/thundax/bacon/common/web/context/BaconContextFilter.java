package com.github.thundax.bacon.common.web.context;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class BaconContextFilter extends OncePerRequestFilter {

    private final BaconContextResolver baconContextResolver;

    public BaconContextFilter(BaconContextResolver baconContextResolver) {
        this.baconContextResolver = baconContextResolver;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            BaconContextHolder.set(baconContextResolver.resolve(request));
            filterChain.doFilter(request, response);
        } finally {
            BaconContextHolder.clear();
        }
    }
}
