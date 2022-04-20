package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.converters.PublishingStatusRecordConverter;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.exceptions.PublishingStatusNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;

import javax.inject.Inject;
import java.util.List;

public class GetPublishingStatusActivity {

    private PublishingStatusDao publishingStatusDao;

    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao publishingStatusDao) {
        this.publishingStatusDao = publishingStatusDao;
    }

    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) {

        String publishingRecordId = publishingStatusRequest.getPublishingRecordId();

        List<PublishingStatusItem> publishingStatusItems;

        try {
            publishingStatusItems = publishingStatusDao.getPublishingStatuses(publishingRecordId);
        } catch (PublishingStatusNotFoundException e) {
            throw new PublishingStatusNotFoundException("Publishing status not found", e);
        }


        List<PublishingStatusRecord> records = PublishingStatusRecordConverter.toPublishingStatusRecords(publishingStatusItems);

        return GetPublishingStatusResponse.builder()
                .withPublishingStatusHistory(records)
                .build();
    }
}

