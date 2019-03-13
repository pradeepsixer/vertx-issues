package test.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

    public static final String SERVER_CERT = "SERVER_CERT";
    public static final String SERVER_KEY = "SERVER_KEY";

    @Override
    public void start(Future<Void> startFuture) {
        Router router = Router.router(getVertx());

        router.route().failureHandler(rc -> {
            rc.failure().printStackTrace();
            rc.response().setStatusCode(500).end(rc.failure().getMessage());
        });

        router.get("/testtls").handler(rc -> {
            JsonObject result = new JsonObject()
                .put("isSSL", rc.request().isSSL());

            if (rc.request().sslSession() != null) {
                result.put("creationTime", rc.request().sslSession().getCreationTime());
            }

            rc.response().end(result.toBuffer());
        });

        router.routeWithRegex(".*")
            .handler(rc -> {
                rc.response().end("[" + rc.request().uri() + "]");
            });

        HttpServerOptions options = new HttpServerOptions()
            .setPort(config().getInteger("port", 8443))
            .setSsl(true)
            .setKeyCertOptions(new PemKeyCertOptions()
                .addKeyPath(config().getString(SERVER_KEY))
                .addCertPath(config().getString(SERVER_CERT)))
            .setSni(true);
        HttpServer server = getVertx().createHttpServer(options);
        server.requestHandler(router::accept).listen(listenAr -> {
            if (listenAr.failed()) {
                listenAr.cause().printStackTrace();
            }
            startFuture.complete();
        });
    }
}

