package ch.ethz.inf.vs.a2.solution.http;

import ch.ethz.inf.vs.a2.http.HttpRawRequest;

/**
 * Created by Joel on 17.10.2016.
 */

public class HttpRawRequestImpl implements HttpRawRequest {
    @Override
    public String generateRequest(String host, int port, String path) {

        /* Builds one long string that gets a useful response when fired from my browser extension,
        passes the unit test and ends up looking like this:

        GET /sunspots/Spot1/sensors/temperature/ HTTP/1.1
        Host: vslab.inf.ethz.ch:8081
        Accept: text/html
        Cache-Control: no-cache
        Connection: close

        For the parameters  host = "vslab.inf.ethz.ch"
                            port = 8081
                            path = "/sunspots/Spot1/sensors/temperature/"

        I used a StringBuilder because I think that's what you're supposed to do when you keep
        adding to the same String, because String Objects are immutable and it would cause a lot
        of unnecessary allocations with the normal + concatenation.*/

        return new StringBuilder(256).append("GET ").append(path).append(" HTTP/1.1\r\n")
                .append("Host: ").append(host).append(":").append(String.valueOf(port)).append("\r\n")
                .append("Accept: text/html\r\n")
                .append("Cache-Control: no-cache\r\n")
                .append("Connection: close\r\n\r\n").toString();
    }
}
