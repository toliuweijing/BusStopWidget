package network;

import com.google.common.collect.Lists;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class Request {
    public static final String HOST_OBANYC_COM = "api.prod.obanyc.com";
    public static final String QUERY_STOP_MONITORING_XML = "/api/siri/stop-monitoring.json";
    public static final int PORT = 80;
    public static final String VALUE_KEY = "cfb3c75b-5a43-4e66-b7f8-14e666b0c1c1";
    public static final String PARAM_KEY = "key";
    public static final String PARAM_MONITORING_REF = "MonitoringRef";
    public static final String PARAM_LINE_REF = "LineRef";

    public static final int SAMPLE_STOP_CODE = 300067;
    public static final String SAMPLE_LINE_REF = "MTA NYCT_B9";

    public static class Builder {
        String stopCode;
        String lineRef;

        public Builder(int stopCode, String lineRef) {
            this.stopCode = String.valueOf(stopCode);
            this.lineRef = lineRef;
        }

        public URI siriRequest() {
            List<BasicNameValuePair> params = Lists.newArrayList(
                    new BasicNameValuePair(PARAM_KEY, VALUE_KEY),
                    new BasicNameValuePair(PARAM_MONITORING_REF, stopCode),
                    new BasicNameValuePair(PARAM_LINE_REF, lineRef));

            URI uri = null;
            try {
                uri = new URIBuilder()
                        .setScheme("http")
                        .setHost(HOST_OBANYC_COM)
                        .setPort(PORT)
                        .setPath(QUERY_STOP_MONITORING_XML)
                        .addParameter(PARAM_KEY, VALUE_KEY)
                        .addParameter(PARAM_MONITORING_REF, stopCode)
                        .addParameter(PARAM_LINE_REF, lineRef)
                        .build();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return uri;
        }
    }
}
