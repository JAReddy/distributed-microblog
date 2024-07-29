package edu.sjsu.cmpe272.simpleblog.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageRequest {
    LocalDateTime date;
    String author;
    String message;
    String attachment;
    String signature;

    public MessageRequest() {

    }
}
