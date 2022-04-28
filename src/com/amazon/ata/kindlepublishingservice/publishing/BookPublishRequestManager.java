package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BookPublishRequestManager {

    private final Queue<BookPublishRequest> publishRequests = new ConcurrentLinkedQueue<>();


    @Inject
    public BookPublishRequestManager() {

    }


    public void addBookPublishRequest(BookPublishRequest book) {
        publishRequests.add(book);
    }

    public BookPublishRequest getBookPublishRequestToProcess() {
        if(publishRequests.isEmpty()) {
            return null;
        }

        return publishRequests.remove();
    }







}
