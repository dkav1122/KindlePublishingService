package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.requests.SubmitBookForPublishingRequest;
import com.amazon.ata.kindlepublishingservice.models.response.SubmitBookForPublishingResponse;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;
import com.amazon.ata.recommendationsservice.types.BookGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class SubmitBookForPublishingActivityTest {

    @Mock
    private PublishingStatusDao publishingStatusDao;

    @Mock
    private CatalogDao catalogDao;

    @Mock
    private BookPublishRequestManager bookPublishRequestManager;


    @InjectMocks
    private SubmitBookForPublishingActivity activity;

    @BeforeEach
    public void setup() {
       initMocks(this);
    }

    @Test
    public void execute_bookIdInRequest_bookQueuedForPublishing() {
        // GIVEN
        SubmitBookForPublishingRequest request = SubmitBookForPublishingRequest.builder()
                .withAuthor("Author")
                .withTitle("Title")
                .withBookId("book.123")
                .withGenre(BookGenre.FANTASY.name())
                .build();

        PublishingStatusItem item = new PublishingStatusItem();
        item.setPublishingRecordId("publishing.123");
        // KindlePublishingUtils generates a random publishing status ID for us
        when(publishingStatusDao.setPublishingStatus(anyString(),
                eq(PublishingRecordStatus.QUEUED),
                eq(request.getBookId()))).thenReturn(item);
        ArgumentCaptor<BookPublishRequestManager> captor = ArgumentCaptor.forClass(BookPublishRequestManager.class);
        doNothing().when(bookPublishRequestManager).addBookPublishRequest(any(BookPublishRequest.class));



        // WHEN
        SubmitBookForPublishingResponse response = activity.execute(request);

        // THEN
//        verify(bookPublishRequestManager, times(1));

        assertEquals("publishing.123", response.getPublishingRecordId(), "Expected response to return a publishing" +
                "record id.");
    }

    @Test
    public void execute_noBookIdInRequest_bookQueuedForPublishing() {
        // GIVEN
        SubmitBookForPublishingRequest request = SubmitBookForPublishingRequest.builder()
                .withAuthor("Author")
                .withTitle("Title")
                .withGenre(BookGenre.FANTASY.name())
                .build();

        PublishingStatusItem item = new PublishingStatusItem();
        item.setPublishingRecordId("publishing.123");
        when(publishingStatusDao.setPublishingStatus(anyString(),
                eq(PublishingRecordStatus.QUEUED),
                isNull())).thenReturn(item);

        // WHEN
        SubmitBookForPublishingResponse response = activity.execute(request);

        // THEN
        assertEquals("publishing.123", response.getPublishingRecordId(), "Expected response to return a publishing" +
                "record id.");
    }


    @Test
    public void execute_BookIdDoesNotExist_throwsException() {
        SubmitBookForPublishingRequest request = SubmitBookForPublishingRequest.builder()
                .withAuthor("Author")
                .withTitle("Title")
                .withBookId("book.123")
                .withGenre(BookGenre.FANTASY.name())
                .build();

       doThrow(BookNotFoundException.class).when(catalogDao).validateBookExists(request.getBookId());

       //WHEN + THEN

        assertThrows(BookNotFoundException.class, () -> activity.execute(request));
        verify(catalogDao, times(1)).validateBookExists(request.getBookId());
        verify(catalogDao, never()).removeBookFromCatalog(request.getBookId());
    }

//    @Test
//    public void execute_BookIdDoesExist_OldVersionIsMadeInactive() {
//
//        //GIVEN
//        SubmitBookForPublishingRequest request = SubmitBookForPublishingRequest.builder()
//                .withAuthor("Author")
//                .withTitle("Title")
//                .withBookId("book.123")
//                .withGenre(BookGenre.FANTASY.name())
//                .build();
//
//        doNothing().when(catalogDao).validateBookExists(request.getBookId());
//
//        PublishingStatusItem item = new PublishingStatusItem();
//        item.setPublishingRecordId("publishing.123");
//        when(publishingStatusDao.setPublishingStatus(anyString(),
//                eq(PublishingRecordStatus.QUEUED),
//                isNull())).thenReturn(item);
//
//       when(publishingStatusItem.getPublishingRecordId()).thenReturn(any(String.class));
//
//        //WHEN
//        SubmitBookForPublishingResponse response = activity.execute(request);
//
//
//        //THEN
//        verify(catalogDao, times(1)).removeBookFromCatalog(request.getBookId());
//        assertEquals("publishing.123", response.getPublishingRecordId(), "Expected response to return a publishing" +
//                "record id.");
//    }



}
