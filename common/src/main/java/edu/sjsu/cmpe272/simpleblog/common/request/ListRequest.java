package edu.sjsu.cmpe272.simpleblog.common.request;

import lombok.Data;

@Data
public class ListRequest {
    Integer limit = 10;
    Long next;
    Integer page = 0;
    String user;
    Boolean includeFollowers;
}
