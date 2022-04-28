package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;

import javax.inject.Inject;

public class BookPublishTask implements Runnable {

    //make final
    private final BookPublishRequestManager bookPublishRequestManager;
    private final PublishingStatusDao publishingStatusDao;
    private final CatalogDao catalogDao;

    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager, PublishingStatusDao publishingStatusDao, CatalogDao catalogDao) {
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
    }


    public void run() {


            BookPublishRequest bookPublishRequest = bookPublishRequestManager.getBookPublishRequestToProcess();
            if (bookPublishRequest == null) {
                return;
            }

            String bookRecordId = bookPublishRequest.getPublishingRecordId();
            String bookId = bookPublishRequest.getBookId();

        try{
            //set Publishing status in progress
            publishingStatusDao.setPublishingStatus(bookRecordId, PublishingRecordStatus.IN_PROGRESS, bookId);

            //format to KindleBook
            KindleFormattedBook kindleFormattedBook = KindleFormatConverter.format(bookPublishRequest);

            //call to catalog Dao to add or update existing book
            try {
                CatalogItemVersion catalogItemVersion = catalogDao.createOrUpdateBook(kindleFormattedBook);
            } catch (BookNotFoundException e) {
                publishingStatusDao.setPublishingStatus(bookRecordId, PublishingRecordStatus.FAILED, bookId, "Book to update does not exist");
            }

        } catch (Exception e) {
            publishingStatusDao.setPublishingStatus(bookRecordId, PublishingRecordStatus.FAILED, bookId, "Exception during processing");
        }

        publishingStatusDao.setPublishingStatus(bookRecordId, PublishingRecordStatus.SUCCESSFUL, bookId);

    }


}
