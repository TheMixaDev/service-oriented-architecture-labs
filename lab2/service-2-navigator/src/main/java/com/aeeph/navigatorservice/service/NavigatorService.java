package com.aeeph.navigatorservice.service;

import com.aeeph.navigatorservice.model.Route;
import java.util.Optional;

public interface NavigatorService {
    Optional<Route> findOptimalRoute(long fromId, long toId, boolean shortest);
    Route createRouteByIds(long fromId, long toId, int distance);
}
