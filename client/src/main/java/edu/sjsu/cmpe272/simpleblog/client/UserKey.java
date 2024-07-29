package edu.sjsu.cmpe272.simpleblog.client;

import lombok.Data;

@Data
public class UserKey {
    private String userId;
    private String key;

    UserKey() {

    }
    UserKey(String id, String privateKey) {
        userId = id;
        key = privateKey;
    }
}
