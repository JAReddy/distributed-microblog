package edu.sjsu.cmpe272.simpleblog.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.sjsu.cmpe272.simpleblog.common.request.MessageRequest;
import edu.sjsu.cmpe272.simpleblog.common.response.FollowSuccess;
import edu.sjsu.cmpe272.simpleblog.common.response.MessageSuccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class Util {

    KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Adjust key size as needed
        return keyPairGenerator.generateKeyPair();
    }

    void saveToMbIni(String userId, PrivateKey privateKey) {
        // Convert private key to Base64 format for storage
        String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        // Save user ID and private key to mb.ini file
        try (FileWriter writer = new FileWriter("mb.ini")) {
            writer.write("User ID: " + userId + "\n");
            writer.write("Private Key: " + privateKeyBase64 + "\n");
        } catch (Exception e) {
            log.error("Error while saving keys to mb.ini: \n {}", e.getMessage());
        }
    }

    UserKey getUserKey() {
        String filePath = "mb.ini";

        // Variables to store userId and private key
        UserKey userKey = new UserKey();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line into key and value using ":" as delimiter
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    if (key.equals("User ID")) {
                        userKey.setUserId(value);
                    } else if (key.equals("Private Key")) {
                        userKey.setKey(value);
                    }
                }
            }
            return userKey;
        } catch (Exception e) {
            log.error("Error while fetching user key from mb.ini: {}", e.getMessage());
            return null;
        }
    }

    String signMessageRequest(MessageRequest message, UserKey userKey) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            ObjectNode node = objectMapper.createObjectNode();
            node.put("date", String.valueOf(message.getDate()));
            node.put("author", message.getAuthor());
            node.put("message", message.getMessage());
            node.put("attachment", message.getAttachment());
            String jsonString = objectMapper.writeValueAsString(node);

            // Remove whitespace characters from JSON string
            String compactJsonString = jsonString.replaceAll("\\s", "");

            // Calculate SHA-256 digest
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedMessage = digest.digest(compactJsonString.getBytes());

            PrivateKey privateKey = generatePrivateKeyFromBase64(userKey.getKey());

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(hashedMessage);
            byte[] signedBytes = signature.sign();

            String signedMessageBase64 = Base64.getEncoder().encodeToString(signedBytes);
            return signedMessageBase64;
        } catch (Exception e) {
            log.error("Error while signing message: \n {}", e.getMessage());
            return null;
        }

    }

    PrivateKey generatePrivateKeyFromBase64(String base64PrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode the Base64 encoded private key to byte array
        byte[] privateKeyBytes = Base64.getDecoder().decode(base64PrivateKey);

        // Create a PKCS8EncodedKeySpec from the byte array
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        // Get an RSA key factory
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        // Generate the private key object
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        return privateKey;
    }

    String printMessages(List<MessageSuccess> msgList) {
        StringBuilder sb = new StringBuilder();
        for (MessageSuccess m : msgList) {
            sb.append(m.toString()+"\n");
        }
        return sb.toString();
    }

    String printFollowers(List<FollowSuccess> followSuccessList) {
        StringBuilder sb = new StringBuilder();
        for (FollowSuccess followSuccess : followSuccessList) {
            sb.append(followSuccess.getFollowee()+"\n");
        }
        return sb.toString();
    }

    void saveAttachments(List<MessageSuccess> msgList) {
        for (MessageSuccess m : msgList) {
            try {
                if(m.getAttachment() == null) continue;
                byte[] decodedBytes = Base64.getDecoder().decode(m.getAttachment());
                String fileName = m.getMessageId()+ ".out";
                Files.write(Paths.get(fileName), decodedBytes);
            } catch (IOException e) {
                log.error("Error while saving attachments: {}", e.getMessage());
            }
        }
    }
}