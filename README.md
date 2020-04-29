# Open Telemetry Traces in Logs Prototype

This prototype is a demo of how OpenTelemetry can be extended to support logs. 
This contains both a library and a demo- the library contains exporters for
Logback, Log4j 2.x, and java.util.Logging. The java.util.Logging
is registered manually (though it can be registered via system
properties), the Logback and Log4j forwarders are configured using 
configuration files supplied in the classpath (see `src/main/resources/`)

There is a docker-compose setup to run against in the `docker` directory- 
simply run `docker-compose up` from that directory to get a fluent-bit server,
a Splunk server, and a Jaeger server all running. Jaeger does not need a 
password, and is available on port [16686](http://localhost:16686). Splunk
is on port [8000](http://localhost:8000) and can be accessed with the username
`admin` and the password `betelgeuse` (one of the two navigational stars in
the Orion constellation).

The demo itself is in the class `com.splunk.thundercat.demo.Demo`, and will
simply call a demo class for each of the logging libraries inside of a tracing
context, and each of those methods logs a message within a child tracing
context. 

Once the demo class is run, you should be able to search in Splunk and find
log information with trace information attached.
