package org.hung.pojo;

import java.util.UUID;

import lombok.Data;

@Data
public class Txn {
    
    private UUID txnId = UUID.randomUUID();

    final private UUID accountId;
    final private long seq;
    final private String type;
    final private double amount;
}
