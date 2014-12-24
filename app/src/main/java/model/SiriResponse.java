package model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class SiriResponse {

  @JsonProperty("Siri")
  public Siri siri;

  @JsonIgnoreProperties(ignoreUnknown=true)
  public static class Siri {
    @JsonProperty("ServiceDelivery")
    public ServiceDelivery serviceDelivery;


    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class ServiceDelivery {
      @JsonProperty("StopMonitoringDelivery")
      public List<StopMonitoringDelivery> stopMonitoringDeliveryConnection;


      @JsonIgnoreProperties(ignoreUnknown=true)
      public static class StopMonitoringDelivery {
        @JsonProperty("MonitoredStopVisit")
        public List<MonitoredStopVisit> monitoredStopVisitConnection;


        @JsonIgnoreProperties(ignoreUnknown=true)
        public static class MonitoredStopVisit {
          @JsonProperty("MonitoredVehicleJourney")
          public MonitoredVehicleJourney monitoredVehicleJourney;

          @JsonIgnoreProperties(ignoreUnknown=true)
          public static class MonitoredVehicleJourney {
            @JsonProperty("LineRef")
            public String lineRef;
          }
        }
      }
    }
  }
}


