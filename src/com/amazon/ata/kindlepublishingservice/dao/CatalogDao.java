package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import javax.inject.Inject;
import java.util.List;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public CatalogItemVersion removeBookFromCatalog(String bookId) {
        CatalogItemVersion bookToSoftDelete;

        bookToSoftDelete =  getLatestVersionOfBook(bookId);

        if(bookToSoftDelete == null || bookToSoftDelete.isInactive()) {
            throw new BookNotFoundException("Book with id: " + bookId + "has never existed");
        }

        bookToSoftDelete.setInactive(true);
        return saveCatalogItemVersion(bookToSoftDelete);
    }

    public CatalogItemVersion removeActiveOrInactiveBookFromCatalog(String bookId) {
        CatalogItemVersion bookToSoftDelete;

        bookToSoftDelete =  getLatestVersionOfBook(bookId);

        if(bookToSoftDelete == null) {
            throw new BookNotFoundException("Book with id: " + bookId + "has never existed");
        }

        bookToSoftDelete.setInactive(true);
        return saveCatalogItemVersion(bookToSoftDelete);

    }


    private CatalogItemVersion saveCatalogItemVersion(CatalogItemVersion book) {
        dynamoDbMapper.save(book);
        return book;

    }

    public void validateBookExists(String bookId) {
        CatalogItemVersion book;

        book = this.getLatestVersionOfBook(bookId);

        if(book == null) {
            throw new BookNotFoundException("This book does not and has never existed");
        }
    }
}
