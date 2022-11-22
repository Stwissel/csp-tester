/* (C) 2022, Notessensei Apache 2.0  */
package com.notessensei.csptester;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {
    public static void main(String[] args) {
        MainVerticle mv = new MainVerticle();
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(mv);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        final String portCandidate = System.getenv("PORT");
        final int port = (portCandidate == null || portCandidate.equals("")) ? 8765
                : Integer.parseInt(portCandidate);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(this::handleCSP);
        router.route().handler(StaticHandler.create());
        StaticHandler staticCSP = StaticHandler.create(FileSystemAccess.RELATIVE, "csp")
                .setDirectoryListing(true)
                .setCachingEnabled(false)
                .setDefaultContentEncoding("UTF-8");

        router.route("/csp").handler(staticCSP);
        router.route("/csp/*").handler(staticCSP);

        vertx.createHttpServer().requestHandler(router)
                .listen(port)
                .onSuccess(server -> {
                    System.out.printf("Server running on port %s%n", port);
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);

    }

    void handleCSP(final RoutingContext ctx) {
        HttpServerRequest request = ctx.request();
        System.out.println(request.absoluteURI());
        MultiMap params = request.params();
        final String cspName = params.contains("csp") ? params.get("csp") : "default";
        this.getCsp(cspName)
                .ifPresent(csp -> ctx.response().putHeader("Content-Security-Policy", csp));
        ctx.next();
    }

    private Optional<String> getCsp(String cspName) {
        final String lookupName = String.format("/csp/%s.txt", cspName);
        InputStream in = this.getClass().getResourceAsStream(lookupName);
        if (in == null) {
            in = this.getClass().getResourceAsStream("/csp/default-csp.txt");
        }

        if (in == null) {
            return Optional.empty();
        }

        final String result = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining(" ")).trim();

        if (result.equals("")) {
            return Optional.empty();
        }

        return Optional.of(result);
    }

}
