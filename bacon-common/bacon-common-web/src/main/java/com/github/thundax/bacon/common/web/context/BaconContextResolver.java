package com.github.thundax.bacon.common.web.context;

import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import jakarta.servlet.http.HttpServletRequest;

public interface BaconContextResolver {

    BaconContext resolve(HttpServletRequest request);
}
