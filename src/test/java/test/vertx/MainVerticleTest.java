package test.vertx;

import static test.vertx.MainVerticle.SERVER_CERT;
import static test.vertx.MainVerticle.SERVER_KEY;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {
  private SelfSignedCertificate serverKeyCertPair;
  private Vertx vertx = Vertx.vertx();

  @Before
  public void setup(TestContext context) {
    serverKeyCertPair = SelfSignedCertificate.create("localhost");

    JsonObject config = new JsonObject()
        .put(SERVER_KEY, serverKeyCertPair.privateKeyPath())
        .put(SERVER_CERT, serverKeyCertPair.certificatePath());
    vertx.deployVerticle(new MainVerticle(), new DeploymentOptions().setConfig(config),
        context.asyncAssertSuccess());
  }

  @Test
  public void testSslSessionCreationTime(TestContext context) {
    Async async = context.async(2);
    WebClientOptions clientOptions = new WebClientOptions()
        .setPemTrustOptions(new PemTrustOptions()
            .addCertPath(serverKeyCertPair.certificatePath()));
    WebClient client = WebClient.create(vertx, clientOptions);
    client.getAbs("https://localhost:8443/muhahaha").send(ar -> {
      context.assertTrue(ar.succeeded());
      context.assertEquals(200, ar.result().statusCode());
      context.assertEquals("[/muhahaha]", ar.result().bodyAsString());
      System.out.println("Someone laughs muhahaha");
      async.countDown();
    });

    client.getAbs("https://localhost:8443/testtls").send(ar -> {
      context.assertTrue(ar.succeeded());
      context.assertEquals(200, ar.result().statusCode());
      System.out.println(ar.result().bodyAsString());
      async.countDown();
    });
  }
}
