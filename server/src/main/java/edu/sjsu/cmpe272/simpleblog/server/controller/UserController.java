package edu.sjsu.cmpe272.simpleblog.server.controller;

import edu.sjsu.cmpe272.simpleblog.common.request.FollowRequest;
import edu.sjsu.cmpe272.simpleblog.common.request.UserRequest;
import edu.sjsu.cmpe272.simpleblog.common.response.*;
import edu.sjsu.cmpe272.simpleblog.server.kafka.producer.UserProducer;
import edu.sjsu.cmpe272.simpleblog.server.entity.Follow;
import edu.sjsu.cmpe272.simpleblog.server.entity.User;
import edu.sjsu.cmpe272.simpleblog.server.repository.FollowRepository;
import edu.sjsu.cmpe272.simpleblog.server.repository.UserRepository;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.api.ZkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static edu.sjsu.cmpe272.simpleblog.server.zookeeper.util.ZkDemoUtil.getHostPostOfServer;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserRepository repository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    ZkService zkService;

    @Value("${server.partition.count}")
    private Integer partitionCount;

    @Autowired
    private UserProducer producer;

    @GetMapping("/{userName}/followers")
    public FollowSuccessList getFollowers(@PathVariable String userName) {
        List<Follow> followersList = followRepository.findFollowsByFollowerOrderByFolloweeAsc(userName);
        return convertToFollowSuccessList(followersList);
    }
    @PostMapping("/follow")
    public FollowSuccess followUser(@RequestBody FollowRequest followRequest) {
        String follower = followRequest.getFollower();
        String followee = followRequest.getFollowee();
        Follow follow = new Follow(followRequest);
        Optional<User> existingUser1 = repository.findById(follower);
        Optional<User> existingUser2 = repository.findById(followee);

        if (existingUser1.isEmpty()) {
            return new FollowSuccess("Error: " + follower + " is not a registered user");
        }
        if (existingUser2.isEmpty()) {
            return new FollowSuccess("Error: " + followee + " is not a registered user");
        }

        Follow existingFollow = followRepository.findFollowByFollowerAndFollowee(follower, followee);
        if (existingFollow != null) {
            return new FollowSuccess( "Error: " + follower + " is already following " + followee);
        } else{
            followRepository.save(follow);
        }
        FollowSuccess res = new FollowSuccess(follower, followee, true);
        return res;
    }

    @PostMapping("/unfollow")
    public FollowSuccess unfollowUser(@RequestBody FollowRequest followRequest) {
        String follower = followRequest.getFollower();
        String followee = followRequest.getFollowee();

        Optional<User> existingUser1 = repository.findById(follower);
        Optional<User> existingUser2 = repository.findById(followee);

        if (existingUser1.isEmpty()) {
            return new FollowSuccess("Error: " + follower + " is not a registered user");
        }
        if (existingUser2.isEmpty()) {
            return new FollowSuccess("Error: " + followee + " is not a registered user");
        }

        Follow existingFollow = followRepository.findFollowByFollowerAndFollowee(follower, followee);
        if (existingFollow == null) {
            return new FollowSuccess( "Error: " + follower  + " is not currently following " + followee);
        } else{
            followRepository.delete(existingFollow);
        }
        FollowSuccess res = new FollowSuccess(follower, followee, false);
        return res;
    }

    @PostMapping("/create")
    public UserSuccess createUser(@RequestBody UserRequest user) {
        User usr = new User(user);
        int partition = Math.abs(user.getUser().hashCode() % partitionCount);
        String partitionLeader = zkService.getPartitionLeader(partition);
        if (getHostPostOfServer().equals(partitionLeader)) {
            Optional<User> existingUser = repository.findById(user.getUser());
            if (existingUser.isPresent()) {
                return new UserSuccess("Duplicate Id");
            } else{
                repository.save(usr);
                producer.sendMessage(user, partition);
                return new UserSuccess("welcome");
            }
        } else {
            final String uri = "http://" + partitionLeader + "/user/create";
            RestTemplate restTemplate = new RestTemplate();
            UserSuccess response = restTemplate.postForObject(uri, user, UserSuccess.class);
            return response;
        }
    }

    @GetMapping("/{userName}/public-key")
    public String getPublicKey(@PathVariable String userName) {
        try {
            Optional<User> usr = repository.findById(userName);
            if (usr.isPresent()) {
                byte[] decodedBytes = Base64.getDecoder().decode(usr.get().getPublicKey());
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");

                PublicKey key = keyFactory.generatePublic(keySpec);
                return key.toString();
            } else {
                return "Username not found";
            }
        } catch (Exception e) {
            return "Error while fetching public key";
        }
    }

    private FollowSuccessList convertToFollowSuccessList(List<Follow> followersList) {
        List<FollowSuccess> resList = new ArrayList<>();
        for(Follow follow: followersList) {
            resList.add(new FollowSuccess(follow.getFollower(), follow.getFollowee()));
        }
        FollowSuccessList res = new FollowSuccessList();
        res.setFollowersList(resList);
        return res;
    }
}
