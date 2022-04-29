package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dagger.ApplicationComponent;
import com.amazon.ata.kindlepublishingservice.dagger.DaggerApplicationComponent;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.recommendationsservice.types.BookGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class BookPublishTaskTest {
    private static final ApplicationComponent COMPONENT = DaggerApplicationComponent.create();

    @Mock
    private BookPublishRequestManager requestManager;

    @Mock
    private CatalogDao catalogDao;

    @Mock
    private PublishingStatusDao publishingStatusDao;

    @Mock
    private KindleFormatConverter kindleFormatConverter;



    @BeforeEach
    public void setup() {

        initMocks(this);
    }

    @Test
    public void run_BookRequestManagerIsEmpty_returnsNull() {

        when(requestManager.getBookPublishRequestToProcess()).thenReturn(null);

        BookPublishTask bookPublishTask = new BookPublishTask(requestManager, publishingStatusDao, catalogDao);

        bookPublishTask.run();

        verify(requestManager).getBookPublishRequestToProcess();
        Mockito.verifyNoMoreInteractions(publishingStatusDao);

    }

    @Test
    public void run_BookRequestManagerReturnsRequest_returnsNull() {
        BookPublishRequest bookPublishRequest =  BookPublishRequest.builder().withBookId("TestBookId")
                .withAuthor("testAuthor").withGenre(BookGenre.ACTION).withPublishingRecordId("testRecordId").withText("testText").withTitle("testTitle").build();

        when(requestManager.getBookPublishRequestToProcess()).thenReturn(bookPublishRequest);

        String bookRecordId = bookPublishRequest.getPublishingRecordId();
        String bookId = bookPublishRequest.getBookId();
        PublishingRecordStatus recordStatus = PublishingRecordStatus.IN_PROGRESS;

        PublishingStatusItem item = new PublishingStatusItem();

        CatalogItemVersion catalogItemVersion = new CatalogItemVersion();

        when(publishingStatusDao.setPublishingStatus(bookRecordId, recordStatus, bookId)).thenReturn(item);

        KindleFormattedBook kindleFormattedBook = KindleFormattedBook.builder().build();

        when(KindleFormatConverter.format(bookPublishRequest)).thenReturn(kindleFormattedBook);

        when(catalogDao.createOrUpdateBook(kindleFormattedBook)).thenReturn(catalogItemVersion);

        //WHEN
        BookPublishTask bookPublishTask = new BookPublishTask(requestManager, publishingStatusDao, catalogDao);

        bookPublishTask.run();

        //THEN
        verify(requestManager, times(1)).getBookPublishRequestToProcess();
        verify(publishingStatusDao).setPublishingStatus(bookRecordId, recordStatus, bookId);
        verify(catalogDao).createOrUpdateBook(kindleFormattedBook);

    }





}
