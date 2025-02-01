package umk.jakuburb.mars.Teraformacja.Marsa.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatRecord(@JsonProperty("user") String user, @JsonProperty("msg") String msg) {
}
