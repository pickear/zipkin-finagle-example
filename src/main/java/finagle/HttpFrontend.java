package finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.zipkin.core.SamplingTracer;
import com.twitter.util.Future;
import zipkin.finagle.http.HttpZipkinTracer;
import zipkin.finagle.kafka.KafkaZipkinTracer;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class HttpFrontend extends Service<Request, Response> {

  final Service<Request, Response> backendClient;

  HttpFrontend(Service<Request, Response> backendClient) {
    this.backendClient = backendClient;
  }

  @Override
  public Future<Response> apply(Request request) {
    if (request.getUri().equals("/")) {
      return backendClient.apply(Request.apply("/api"));
    }
    Response response = Response.apply();
    response.setStatusCode(404);
    return Future.value(response);
  }

  public static void main(String[] args) {
    // The frontend makes a sampling decision (via Trace.letTracerAndId) and propagates it downstream.
    // This property says sample 100% of traces.

    // All servers need to point to the same zipkin transport
    System.setProperty("zipkin.http.host", "10.50.101.72:9411"); // default

    // It is unreliable to rely on implicit tracer config (Ex sometimes NullTracer is used).
    // Always set the tracer explicitly. The default constructor reads from system properties.
     SamplingTracer tracer = new HttpZipkinTracer();

    Service<Request, Response> backendClient =
        ClientBuilder.safeBuild(ClientBuilder.get()
            .tracer(tracer)
            .codec(Http.get().enableTracing(true))
            .hosts("localhost:9000")
            .hostConnectionLimit(1)
            .name("frontend")); // this assigns the local service name

    ServerBuilder.safeBuild(
        new HttpFrontend(backendClient),
        ServerBuilder.get()
            .tracer(tracer)
            .codec(Http.get().enableTracing(true))
            .bindTo(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8082))
            .name("frontend")); // this assigns the local service name
  }
}
