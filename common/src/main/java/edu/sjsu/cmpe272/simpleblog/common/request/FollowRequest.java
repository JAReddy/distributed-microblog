package edu.sjsu.cmpe272.simpleblog.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FollowRequest {
    String follower;
    String followee;

    public FollowRequest(String follower, String followee) {
        this.follower = follower;
        this.followee = followee;
    }
    public FollowRequest() {

    }
}
