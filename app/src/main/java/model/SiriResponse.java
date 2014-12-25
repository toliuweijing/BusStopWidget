package model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class SiriResponse {

  public static SiriResponse read(JSONObject jsonObject) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper.readValue(jsonObject.toString(), SiriResponse.class);
  }

  @JsonProperty("Siri")
  public Siri siri;

  public static class Siri {
    @JsonProperty("ServiceDelivery")
    public ServiceDelivery serviceDelivery;


    public static class ServiceDelivery {
      @JsonProperty("StopMonitoringDelivery")
      public List<StopMonitoringDelivery> stopMonitoringDeliveryConnection;


      public static class StopMonitoringDelivery {
        @JsonProperty("MonitoredStopVisit")
        public List<MonitoredStopVisit> monitoredStopVisitConnection;


        public static class MonitoredStopVisit {
          @JsonProperty("MonitoredVehicleJourney")
          public MonitoredVehicleJourney monitoredVehicleJourney;

          public static class MonitoredVehicleJourney {
            @JsonProperty("LineRef")
            public String lineRef;

            @JsonProperty("DirectionRef")
            public int directionRef;

            @JsonProperty("DestinationName")
            public String destinationName;

            @JsonProperty("MonitoredCall")
            public MonitoredCall monitoredCall;

            public static class MonitoredCall {
              @JsonProperty("Extensions")
              public Extensions extensions;

              public static class Extensions {
                @JsonProperty("Distances")
                public Distances distances;

                public static class Distances {
                  @JsonProperty("PresentableDistance")
                  public String presentableDistance;

                  @JsonProperty("StopsFromCall")
                  public int stopsFromCall;
                }
              }
            }

          }
        }
      }
    }
  }
}


