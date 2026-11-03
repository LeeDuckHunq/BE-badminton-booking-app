package com.example.alopoapi.service;

import com.example.alopoapi.dto.TopCumSanDTO;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThongKeService {

    private final MongoTemplate mongoTemplate;

    public ThongKeService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<TopCumSanDTO> getTop10CumSanDatNhieuNhat() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.lookup("San", "maSan", "_id", "san"),
                Aggregation.unwind("san"),
                Aggregation.group("san.maCumSan").count().as("soLuongDat"),
                Aggregation.sort(Sort.Direction.DESC, "soLuongDat"),
                Aggregation.limit(10)
        );

        return mongoTemplate.aggregate(
                        aggregation,
                        "PhieuDatSan",
                        Document.class
                )
                .getMappedResults()
                .stream()
                .map(document -> new TopCumSanDTO(
                        document.getString("_id"),
                        ((Number) document.get("soLuongDat")).longValue()
                ))
                .toList();
    }
}
