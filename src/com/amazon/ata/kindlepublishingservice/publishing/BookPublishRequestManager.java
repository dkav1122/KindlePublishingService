package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;

public class BookPublishRequestManager {

    private Queue<BookPublishRequest> publishRequests = new LinkedList<>();


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
