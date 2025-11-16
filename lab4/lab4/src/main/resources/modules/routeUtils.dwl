%dw 2.0
ns ns0 http://aeeph.com/routeservice

fun restRoutePayload(body) =
    if ((body default {}) is Object and (body.Route?))
        body.Route
    else
        body

fun soapRouteToRest(route) =
    if (route == null)
        null
    else
        do {
            var coordinates = route.ns0#coordinates default {}
            var fromLoc = route.ns0#from default {}
            var toLoc = route.ns0#to default {}
            ---
            {
                (id: route.ns0#id) if (route.ns0#id?),
                name: route.ns0#name,
                (creationDate: route.ns0#creationDate) if (route.ns0#creationDate?),
                coordinates: {
                    (x: coordinates.ns0#x) if (coordinates.ns0#x?),
                    (y: coordinates.ns0#y) if (coordinates.ns0#y?)
                },
                from: {
                    (x: fromLoc.ns0#x) if (fromLoc.ns0#x?),
                    (y: fromLoc.ns0#y) if (fromLoc.ns0#y?),
                    name: fromLoc.ns0#name
                },
                to: {
                    (x: toLoc.ns0#x) if (toLoc.ns0#x?),
                    (y: toLoc.ns0#y) if (toLoc.ns0#y?),
                    name: toLoc.ns0#name
                },
                (distance: route.ns0#distance) if (route.ns0#distance?),
                (priority: route.ns0#priority) if (route.ns0#priority?)
            }
        }

fun soapRoutesToRest(routes) =
    if (routes is Array)
        routes map ((item) -> soapRouteToRest(item))
    else if (routes is Object)
        [soapRouteToRest(routes)]
    else
        []

fun buildRouteFields(routePayload) =
    do {
        var route = restRoutePayload(routePayload) default {}
        var coordinates = route.coordinates default {}
        var fromLoc = route.from default {}
        var toLoc = route.to default {}
        ---
        {
            ns0#name: route.name,
            ns0#coordinates: {
                (ns0#x: coordinates.x) if (coordinates.x?),
                ns0#y: coordinates.y
            },
            ns0#from: {
                (ns0#x: fromLoc.x) if (fromLoc.x?),
                (ns0#y: fromLoc.y) if (fromLoc.y?),
                ns0#name: fromLoc.name
            },
            ns0#to: {
                (ns0#x: toLoc.x) if (toLoc.x?),
                (ns0#y: toLoc.y) if (toLoc.y?),
                ns0#name: toLoc.name
            },
            (ns0#distance: route.distance) if (route.distance? and route.distance != null),
            (ns0#priority: route.priority) if (route.priority? and route.priority != null)
        }
    }

