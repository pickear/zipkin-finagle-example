package finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.zipkin.core.SamplingTracer;
import com.twitter.util.Future;
import finagle.filter.TracingFilter;
import zipkin.finagle.kafka.KafkaZipkinTracer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;

public class KafkaBackend extends Service<Request, Response> {

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
      System.setProperty("zipkin.kafka.bootstrapServers", "192.168.115.102:9092,192.168.115.106:9099");
      System.setProperty("zipkin.kafka.topic", "zipkin");

    // It is unreliable to rely on implicit tracer config (Ex sometimes NullTracer is used).
    // Always set the tracer explicitly. The default constructor reads from system properties.
      
      SamplingTracer tracer = new KafkaZipkinTracer();

      TracingFilter filter = new TracingFilter(tracer);
      Service service = new KafkaBackend();
      service = filter.andThen(service);

      ServerBuilder.safeBuild(service,ServerBuilder.get()
                                                 .tracer(tracer)
                                                 .codec(Http.get().enableTracing(true))
                                                 .bindTo(new InetSocketAddress(InetAddress.getLoopbackAddress(), 9000))
                                                 .name("backend")
                              ); // this assigns the local service name
  }
}
