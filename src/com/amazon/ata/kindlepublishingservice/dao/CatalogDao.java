package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;
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

    public CatalogItemVersion createOrUpdateBook(KindleFormattedBook kindleBook) {
        CatalogItemVersion catalogItemVersion;

        if(kindleBook.getBookId() == null) {
            catalogItemVersion = this.addNewKindleBook(kindleBook);
        } else {
            try {
                catalogItemVersion = this.updateExistingKindleBook(kindleBook);
            } catch (BookNotFoundException e) {
                throw new BookNotFoundException("Book to update not found", e);
            }
        }

        return catalogItemVersion;

    }

    private CatalogItemVersion addNewKindleBook(KindleFormattedBook book) {
        CatalogItemVersion catalogItemVersion = new CatalogItemVersion();
        catalogItemVersion.setBookId(KindlePublishingUtils.generateBookId());
        catalogItemVersion.setVersion(1);
        catalogItemVersion.setInactive(false);
        catalogItemVersion.setAuthor(book.getAuthor());
        catalogItemVersion.setGenre(book.getGenre());
        catalogItemVersion.setText(book.getText());
        catalogItemVersion.setTitle(book.getTitle());

        saveCatalogItemVersion(catalogItemVersion);


        return catalogItemVersion;
       
    }


    private CatalogItemVersion updateExistingKindleBook(KindleFormattedBook book) {

        CatalogItemVersion catalogItemVersion = this.getLatestVersionOfBook(book.getBookId());

        if(catalogItemVersion == null) {
            throw new BookNotFoundException("Book to update with id:" + book.getBookId() + " does not Exist");
        }

        //set previous version inactive
        this.removeActiveOrInactiveBookFromCatalog(book.getBookId());

        //update catalog item version by 1
        int currentVersion = catalogItemVersion.getVersion();

        catalogItemVersion.setVersion(catalogItemVersion.getVersion() + 1);
        catalogItemVersion.setBookId(catalogItemVersion.getBookId());
        catalogItemVersion.setAuthor(catalogItemVersion.getAuthor());
        catalogItemVersion.setInactive(false);
        catalogItemVersion.setTitle(catalogItemVersion.getTitle());
        catalogItemVersion.setText(catalogItemVersion.getText());
        catalogItemVersion.setGenre(catalogItemVersion.getGenre());



        //save updated catalog item to data store
       catalogItemVersion = saveCatalogItemVersion(catalogItemVersion);
       return catalogItemVersion;

    }
}
