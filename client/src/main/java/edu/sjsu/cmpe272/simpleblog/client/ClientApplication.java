package edu.sjsu.cmpe272.simpleblog.client;

import edu.sjsu.cmpe272.simpleblog.common.request.FollowRequest;
import edu.sjsu.cmpe272.simpleblog.common.request.MessageRequest;
import edu.sjsu.cmpe272.simpleblog.common.request.ListRequest;
import edu.sjsu.cmpe272.simpleblog.common.request.UserRequest;
import edu.sjsu.cmpe272.simpleblog.common.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.IFactory;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;


@SpringBootApplication
@Command
@Slf4j
public class ClientApplication implements CommandLineRunner, ExitCodeGenerator {

    @Autowired
    IFactory iFactory;

    @Value("${serverUrl}")
    private String serverUrl;

    @Autowired
    private Util util;

    @Command(name = "list", description = "list messages")
    public int list(
            @Parameters(index = "0", description = "User name") String user,
            @Option(names = {"--starting-id"}, defaultValue = "-1", description = "Starting Id to list the messages") Long start,
            @Option(names = {"--count-number"}, defaultValue = "10", description = "Number of messages to return") Integer count,
            @Option(names = {"--save-attachment"}, description = "To create a file with the base64 decoded attachment named message-id.out") Boolean saveAttachment,
            @Option(names = {"--followers"}, description = "If given, the posts of users followed by the current user is included in the list") Boolean includeFollowers
    ) {
        try {
            final String uri = serverUrl + "/messages/list";
            List<MessageSuccess> msgList = new ArrayList<>();
            ListRequest request = new ListRequest();
            if (start == -1) {
                request.setNext(1000000000L);
            } else {
                request.setNext(start);
            }
            RestTemplate restTemplate = new RestTemplate();
            int page = 0;
            while (count > 0) {
                if (count < 20) {
                    request.setLimit(count);
                } else {
                    request.setLimit(20);
                }
                request.setPage(page);
                request.setUser(user);
                request.setIncludeFollowers(includeFollowers != null && includeFollowers);
                MessageSuccessList response = restTemplate.postForObject(uri, request, MessageSuccessList.class);

                if (response != null && !response.getMsgSuccessList().isEmpty()) {
                    msgList.addAll(response.getMsgSuccessList());
                }
                count-=20;
                page++;
            }
            if (msgList.isEmpty()) {
                log.info("No messages to display");
                System.out.println("No messages to display");
                return 0;
            }
            if (saveAttachment!= null && saveAttachment) {
                util.saveAttachments(msgList);
            }

            System.out.println(util.printMessages(msgList));
            return 0;
        } catch (Exception e) {
            log.error("Error while listing the messages: {}", e.getMessage());
            return -1;
        }
    }
    @Command(name = "post", description = "Post a message")
    public int post(@Parameters String message, @Parameters(defaultValue = "null") String attachment) {
        try {
            final String uri = serverUrl + "/messages/create";
            MessageRequest request = new MessageRequest();
            UserKey userKey =  util.getUserKey();
//            if (userKey == null) {
//                System.out.println("User not registered, please create user and then post a message");
//                return -1;
//            }
//
//            final String verifyUrl = serverUrl+"/user/"+userKey.getUserId()+"/public-key";
            RestTemplate restTemplate = new RestTemplate();
//
//            String verificationMsg = String.valueOf(restTemplate.getForEntity(verifyUrl, String.class));
//
//            if (verificationMsg != null && verificationMsg.contains("Username not found")) {
//                String msg = "Unauthorized user, please create a new user for this client and delete any mb.ini file in current directory";
//                log.error(msg);
//                System.out.println(msg);
//                return -1;
//            }

            if (!attachment.equals("null")) {
                File file = new File(attachment);
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                String encodedAttachment = Base64.getEncoder().encodeToString(fileBytes);
                request.setAttachment(encodedAttachment);
            } else {
                request.setAttachment(null);
            }

            request.setDate(LocalDateTime.now());
            request.setAuthor(userKey.getUserId());
            request.setMessage(message);

            request.setSignature(util.signMessageRequest(request, userKey));

            MessageSuccess response = restTemplate.postForObject(uri, request, MessageSuccess.class);

            if(response == null || response.getMessageId() == null) {
                log.error("Error while posting the message to the server");
                return -1;
            } else {
                log.info("Message with Id {} is saved to database", response.getMessageId());
                System.out.println("Message with Id " + response.getMessageId()+ " is saved to database");
            }
        } catch (Exception e) {
            log.error("Error while posting message {}", e.getMessage());
            return -1;
        }

        return 1;
    }

    @Command(name = "follow", description = "Will cause the current user (indicated in mb.ini) to follow the specified userid")
    int follow(@Parameters String followee) {
        try{
            UserKey userKey =  util.getUserKey();
            if (userKey == null) {
                System.out.println("User not registered, please create user and then follow");
                return -1;
            }
            FollowRequest request = new FollowRequest(userKey.getUserId(), followee);
            final String uri = serverUrl+"/user/follow";
            RestTemplate restTemplate = new RestTemplate();
            FollowSuccess response = restTemplate.postForObject(uri, request, FollowSuccess.class);
            if(response == null) {
                log.error("Cannot follow users right now, server might be down.");
                return -1;
            } else if (response.getMessage().contains("Error")){
                System.out.println(response.getMessage());
                log.error(response.getMessage());
                return -1;
            }
            else {
                System.out.println(response.getMessage());
                return 0;
            }

        } catch (Exception e) {
            log.error("Error while following user : \n {}", e.getMessage());
            exitCode = -1;
            return exitCode;
        }
    }

    @Command(name = "unfollow", description = "Will cause the current user (indicated in mb.ini) to unfollow the specified userid")
    int unfollow(@Parameters String followee) {
        try{
            UserKey userKey =  util.getUserKey();
            if (userKey == null) {
                System.out.println("User not registered, please create user and then follow");
                return -1;
            }
            FollowRequest request = new FollowRequest(userKey.getUserId(), followee);
            final String uri = serverUrl+"/user/unfollow";
            RestTemplate restTemplate = new RestTemplate();
            FollowSuccess response = restTemplate.postForObject(uri, request, FollowSuccess.class);
            if(response == null) {
                log.error("Cannot unfollow users right now, server might be down.");
                return -1;
            } else if (response.getMessage().contains("Error")){
                System.out.println(response.getMessage());
                log.error(response.getMessage());
                return -1;
            }
            else {
                System.out.println(response.getMessage());
                return 0;
            }
        } catch (Exception e) {
            log.error("Error while unfollowing user : \n {}", e.getMessage());
            exitCode = -1;
            return exitCode;
        }
    }

    @Command(name = "list-followers", description = "will list users that the current user follows")
    int listFollowers() {
        try{
            UserKey userKey =  util.getUserKey();
            if (userKey == null) {
                System.out.println("User not registered, please create user and then list the followers");
                return -1;
            }
            final String uri = serverUrl+ "/user/" + userKey.getUserId() + "/followers";
            RestTemplate restTemplate = new RestTemplate();
            FollowSuccessList response = restTemplate.getForObject(uri, FollowSuccessList.class);

            if (response == null) {
                log.error("Cannot list the followers right now, server might be down.");
                return -1;
            } else if (response.getFollowersList().isEmpty()){
                System.out.println("No followers currently for the user");
                return 0;
            }
            else {
                System.out.println(util.printFollowers(response.getFollowersList()));
                return 0;
            }
        } catch (Exception e) {
            log.error("Error while listing followers of the user : \n {}", e.getMessage());
            exitCode = -1;
            return exitCode;
        }
    }

    @Command(name = "create", description = "Create a user")
    int create(@Parameters String id) {
        try {
            UserKey userKey =  util.getUserKey();

            if (userKey != null) {
                String msg = "User is already created for this client";
                log.error(msg);
                System.out.println(msg);
                return -1;
            }

//            final String verifyUrl = serverUrl+"/user/"+id+"/public-key";
            RestTemplate restTemplate = new RestTemplate();
//            String verificationMsg = String.valueOf(restTemplate.getForEntity(verifyUrl, String.class));
//
//            if (verificationMsg != null && !verificationMsg.contains("Username not found")) {
//                String msg = "Duplicate userId";
//                log.error(msg);
//                System.out.println(msg);
//                return -1;
//            }

            final String uri = serverUrl + "/user/create";
            // Generate Key Pair
            KeyPair keyPair = util.generateKeyPair();
            String regex = "^[a-z0-9]+$";
            if (!Pattern.matches(regex, id)) {
                log.error("User Id should only contain lower case alphabets and number");
                return -1;
            }

            // Save User ID and Private Key to mb.ini file
            util.saveToMbIni(id, keyPair.getPrivate());

            String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            UserRequest request = new UserRequest(id, publicKeyBase64);
            UserSuccess response = restTemplate.postForObject(uri, request, UserSuccess.class);

            if(response == null || response.getMessage() == null) {
                log.error("Error while saving the user details in the server");
                exitCode = -1;
                return exitCode;
            }

            String res = "User with Id "+ id +" is created";
            System.out.println(res);
            return exitCode;
        } catch (Exception e) {
            log.error("Error while creating user : \n {}", e.getMessage());
            exitCode = -1;
            return exitCode;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    int exitCode;

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(this, iFactory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

}
