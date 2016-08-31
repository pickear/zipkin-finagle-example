package finagle.filter;

import com.twitter.finagle.Service;
import com.twitter.finagle.SimpleFilter;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.tracing.Trace;
import com.twitter.finagle.tracing.Tracer;
import com.twitter.util.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TracingFilter extends SimpleFilter<Request, Response>{

    private final static Logger log = LoggerFactory.getLogger(TracingFilter.class);

	private Tracer tracer;
	public TracingFilter(Tracer tracer)
	{
		this.tracer = tracer;
	}

    @Override
    public Future<Response> apply(Request request, Service<Request, Response> service) {
        log.info("[filter]traceId : {},spanId : {}", Trace.id().traceId().toString(), Trace.id().spanId().toString());

        return service.apply(request);
    }
}
