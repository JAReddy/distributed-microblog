package edu.sjsu.cmpe272.simpleblog.server.entity;

import edu.sjsu.cmpe272.simpleblog.common.request.FollowRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String follower;
    String followee;
    public Follow(FollowRequest followRequest) {
        follower = followRequest.getFollower();
        followee = followRequest.getFollowee();
    }
    public Follow() {

    }
}
