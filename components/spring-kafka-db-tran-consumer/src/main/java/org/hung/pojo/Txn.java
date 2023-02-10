package org.hung.pojo;

import java.util.UUID;

import lombok.Data;

@Data
public class Txn {
    
    private UUID txnId = UUID.randomUUID();

    private UUID accountId;
    private long seq;
    private String type;
    private double amount;
}
