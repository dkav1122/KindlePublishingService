package com.amazon.ata.kindlepublishingservice.converters;

import com.amazon.ata.coral.converter.CoralConverterUtil;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

import java.util.List;

public class PublishingStatusRecordConverter {

    public PublishingStatusRecordConverter() {
    }


    public static List<PublishingStatusRecord> toPublishingStatusRecords(List<PublishingStatusItem> publishingStatusItems) {

//        List<PublishingStatusRecord> publishingStatusRecords = new ArrayList<>();
//
//        for(PublishingStatusItem item : publishingStatusItems) {
//            PublishingStatusRecord record = toPublishingStatusRecord(item);
//            publishingStatusRecords.add(record);
//        }
//
//        return  publishingStatusRecords;

        return CoralConverterUtil.convertList(publishingStatusItems, PublishingStatusRecordConverter::toPublishingStatusRecord);


    }

    public  static PublishingStatusRecord toPublishingStatusRecord(PublishingStatusItem publishingStatusItem) {

        return PublishingStatusRecord.builder().withStatus(publishingStatusItem.getStatus().toString())
                .withStatusMessage(publishingStatusItem.getStatusMessage())
                .withBookId(publishingStatusItem.getBookId()).build();








    }


        }
