package finagle;

import com.twitter.finagle.Service;
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
import java.util.Date;

public class HttpBackend extends Service<Request, Response> {

  @Override
  public Future<Response> apply(Request request) {
    Response response = Response.apply();
    if (request.path().equals("/api")) {
      response.write(new Date().toString());
    } else {
      response.setStatusCode(404);
    }
    return Future.value(response);
  }

  public static void main(String[] args) {

    // All servers need to point to the same zipkin transport
    System.setProperty("zipkin.http.host", "10.50.101.72:9411"); // default
    // It is unreliable to rely on implicit tracer config (Ex sometimes NullTracer is used).
    // Always set the tracer explicitly. The default constructor reads from system properties.
      
    SamplingTracer tracer = new HttpZipkinTracer();

    ServerBuilder.safeBuild(
        new HttpBackend(),
        ServerBuilder.get()
            .tracer(tracer)
            .codec(Http.get().enableTracing(true))
            .bindTo(new InetSocketAddress(InetAddress.getLoopbackAddress(), 9000))
            .name("backend")); // this assigns the local service name
  }
}
