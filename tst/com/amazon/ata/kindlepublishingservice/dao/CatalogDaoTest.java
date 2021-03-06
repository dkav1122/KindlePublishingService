package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.recommendationsservice.types.BookGenre;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CatalogDaoTest {

    @Mock
    private PaginatedQueryList<CatalogItemVersion> list;

    @Mock
    private DynamoDBMapper dynamoDbMapper;


    @InjectMocks
    private CatalogDao catalogDao;

    @BeforeEach
    public void setup(){
        initMocks(this);
    }

    @Test
    public void getBookFromCatalog_bookDoesNotExist_throwsException() {
        // GIVEN
        String invalidBookId = "notABookID";
        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(true);

        // WHEN && THEN
        assertThrows(BookNotFoundException.class, () -> catalogDao.getBookFromCatalog(invalidBookId),
                "Expected BookNotFoundException to be thrown for an invalid bookId.");
    }

    @Test
    public void getBookFromCatalog_bookInactive_throwsException() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(true);
        item.setBookId(bookId);
        item.setVersion(1);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN && THEN
        assertThrows(BookNotFoundException.class, () -> catalogDao.getBookFromCatalog(bookId),
                "Expected BookNotFoundException to be thrown for an invalid bookId.");
    }

    @Test
    public void getBookFromCatalog_oneVersion_returnVersion1() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(false);
        item.setBookId(bookId);
        item.setVersion(1);
        ArgumentCaptor<DynamoDBQueryExpression> requestCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN
        CatalogItemVersion book = catalogDao.getBookFromCatalog(bookId);

        // THEN
        assertEquals(bookId, book.getBookId());
        assertEquals(1, book.getVersion(), "Expected version 1 of book to be returned");
        assertFalse(book.isInactive(), "Expected book to be active.");

        verify(dynamoDbMapper).query(eq(CatalogItemVersion.class), requestCaptor.capture());
        CatalogItemVersion queriedItem = (CatalogItemVersion) requestCaptor.getValue().getHashKeyValues();
        assertEquals(bookId, queriedItem.getBookId(), "Expected query to look for provided bookId");
        assertEquals(1, requestCaptor.getValue().getLimit(), "Expected query to have a limit set");
    }

    @Test
    public void getBookFromCatalog_twoVersions_returnsVersion2() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(false);
        item.setBookId(bookId);
        item.setVersion(2);
        ArgumentCaptor<DynamoDBQueryExpression> requestCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN
        CatalogItemVersion book = catalogDao.getBookFromCatalog(bookId);

        // THEN
        assertEquals(bookId, book.getBookId());
        assertEquals(2, book.getVersion(), "Expected version 2 of book to be returned");
        assertFalse(book.isInactive(), "Expected book to be active.");

        verify(dynamoDbMapper).query(eq(CatalogItemVersion.class), requestCaptor.capture());
        CatalogItemVersion queriedItem = (CatalogItemVersion) requestCaptor.getValue().getHashKeyValues();
        assertEquals(bookId, queriedItem.getBookId(), "Expected query to look for provided bookId");
        assertEquals(1, requestCaptor.getValue().getLimit(), "Expected query to have a limit set");
    }

    @Test
    public void removeBookFromCatalog_bookDoesNotExist_throwsException() {

        //GIVEN
        String invalidBookId = "notABookID";

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(true);



        //WHEN + THEN

        assertThrows(BookNotFoundException.class, () -> catalogDao.removeBookFromCatalog(invalidBookId),
                "Expected BookNotFoundException to be throw for an invalid bookId.");


    }

    @Test
    public void removeBookFromCatalog_bookIsAlreadyInactive_throwsException() {

        //GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(true);
        item.setBookId(bookId);
        ArgumentCaptor<DynamoDBQueryExpression> requestCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        //WHEN + THEN
        assertThrows(BookNotFoundException.class, () -> catalogDao.removeBookFromCatalog(bookId),
                "Expected BookNotFoundException to be thrown for an invalid bookId.");
    }

    @Test
    public void removeBookFromCatalog_bookIsActive_DeletedBookIsSaved() {
        //GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(false);
        item.setBookId(bookId);
        item.setVersion(2);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        //WHEN
        CatalogItemVersion bookToDelete = catalogDao.removeBookFromCatalog(bookId);

        verify(dynamoDbMapper).save(item);
        assertTrue(item.isInactive(), "Expected item isInactive to be set to [true] but is: " + item.isInactive());

    }

    @Test
    public void createOrUpdateBook_NoBookId_returnsCatalogItemVersion() {

        KindleFormattedBook kindleFormattedBook = KindleFormattedBook.builder().withBookId("")
                .withAuthor("author").withGenre(BookGenre.ACTION).withText("text").withTitle("Title").build();

        CatalogItemVersion catalogItemVersion = new CatalogItemVersion();
        catalogItemVersion.setVersion(1);
        catalogItemVersion.setInactive(false);
        catalogItemVersion.setAuthor("author");
        catalogItemVersion.setGenre(BookGenre.ACTION);
        catalogItemVersion.setText("text");
        catalogItemVersion.setTitle("Title");
        catalogItemVersion.setBookId("111");


      //  when(dynamoDbMapper.save(catalogItemVersion)).thenReturn(catalogItemVersion);


        //WHEN
        CatalogItemVersion item = catalogDao.createOrUpdateBook(kindleFormattedBook);

        //THEN
        assertEquals(catalogItemVersion.getVersion(), item.getVersion());
        assertEquals(catalogItemVersion.getGenre(), item.getGenre());
        assertNotNull(item.getBookId());
    }

    @Test
    public void createOrUpdateBook_ExistingBookId_returnsCatalogItemVersion() {

        KindleFormattedBook kindleFormattedBook = KindleFormattedBook.builder().withBookId("111")
                .withAuthor("author").withGenre(BookGenre.ACTION).withText("text").withTitle("Title").build();

        CatalogItemVersion catalogItemVersion = new CatalogItemVersion();
        catalogItemVersion.setVersion(1);
        catalogItemVersion.setInactive(false);
        catalogItemVersion.setAuthor("author");
        catalogItemVersion.setGenre(BookGenre.ACTION);
        catalogItemVersion.setText("text");
        catalogItemVersion.setTitle("Title");
        catalogItemVersion.setBookId("111");


        when(dynamoDbMapper.query(CatalogItemVersion.class, any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(catalogItemVersion);

        //  when(dynamoDbMapper.save(catalogItemVersion)).thenReturn(catalogItemVersion);


        //WHEN
        CatalogItemVersion item = catalogDao.createOrUpdateBook(kindleFormattedBook);

        //THEN
        assertEquals(catalogItemVersion.getVersion(), item.getVersion());
        assertEquals(catalogItemVersion.getGenre(), item.getGenre());
        assertNotNull(item.getBookId());
    }




}