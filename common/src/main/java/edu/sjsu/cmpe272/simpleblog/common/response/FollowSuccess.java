package edu.sjsu.cmpe272.simpleblog.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FollowSuccess {
    String message;
    String follower;
    String followee;

    public FollowSuccess(String message) {
        this.message = message;
    }
    public FollowSuccess(String follower, String followee, Boolean follow) {
        this.follower = follower;
        this.followee = followee;
        String res = "";
        if (follow) {
           res =  " now follows ";
        } else {
            res = " now unfollows ";
        }
        this.message = "Success! "+ follower + res + followee;
    }

    public FollowSuccess(String follower, String followee) {
        this.follower = follower;
        this.followee = followee;
    }

    public FollowSuccess() {

    }
}
