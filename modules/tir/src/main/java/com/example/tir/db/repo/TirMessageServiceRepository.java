package com.example.tir.db.repo;

import com.example.tir.db.TirMessage;
import java.util.List;

public class TirMessageServiceRepository extends TirMessageRepository {

    public List<TirMessage> findAllSorted() {
        return all().order("-self.createdAt").fetch();
    }
}