package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public final class BookPublishRequestManager {

//    private final Queue<BookPublishRequest> publishRequests = new ConcurrentLinkedQueue<>();
        private final Queue<BookPublishRequest> publishRequests;


    @Inject
    public BookPublishRequestManager(ConcurrentLinkedQueue<BookPublishRequest> linkedQueue) {
        this.publishRequests = linkedQueue;
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
