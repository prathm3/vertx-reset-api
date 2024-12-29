package com.iwlabs.vertx4.reactive.rest.api.api.service;

import com.iwlabs.vertx4.reactive.rest.api.api.model.Book;
import com.iwlabs.vertx4.reactive.rest.api.api.model.BookGetAllResponse;
import com.iwlabs.vertx4.reactive.rest.api.api.model.BookGetByIdResponse;
import com.iwlabs.vertx4.reactive.rest.api.api.repository.BookRepository;
import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.pgclient.PgPool;
import com.iwlabs.vertx4.reactive.rest.api.utils.LogUtils;
import com.iwlabs.vertx4.reactive.rest.api.utils.QueryUtils;

import java.util.List;
import java.util.stream.Collectors;

public class BookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookService.class);

    private final PgPool dbClient;
    private final BookRepository bookRepository;

    public BookService(PgPool dbClient,
                       BookRepository bookRepository) {
        this.dbClient = dbClient;
        this.bookRepository = bookRepository;
    }

    public Future<BookGetAllResponse> readAll(String p,
                                              String l) {
        return dbClient.withTransaction(
                connection -> {
                    final int page = QueryUtils.getPage(p);
                    final int limit = QueryUtils.getLimit(l);
                    final int offset = QueryUtils.getOffset(page, limit);

                    return bookRepository.count(connection)
                            .flatMap(total ->
                                    bookRepository.selectAll(connection, limit, offset)
                                            .map(result -> {
                                                final List<BookGetByIdResponse> books = result.stream()
                                                        .map(BookGetByIdResponse::new)
                                                        .collect(Collectors.toList());

                                                return new BookGetAllResponse(total, limit, page, books);
                                            })
                            );
                })
                .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read all books", success.getBooks())))
                .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read all books", throwable.getMessage())));
    }

    public Future<BookGetByIdResponse> readOne(int id) {
        return dbClient.withTransaction(
                connection -> {
                    return bookRepository.selectById(connection, id)
                            .map(BookGetByIdResponse::new);
                })
                .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read one book", success)))
                .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read one book", throwable.getMessage())));
    }

    public Future<BookGetByIdResponse> create(Book book) {
        return dbClient.withTransaction(
                connection -> {
                    return bookRepository.insert(connection, book)
                            .map(BookGetByIdResponse::new);
                })
                .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Create one book", success)))
                .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Create one book", throwable.getMessage())));
    }

    public Future<BookGetByIdResponse> update(int id,
                                              Book book) {
        book.setId(id);

        return dbClient.withTransaction(
                connection -> {
                    return bookRepository.update(connection, book)
                            .map(BookGetByIdResponse::new);
                })
                .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Update one book", success)))
                .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Update one book", throwable.getMessage())));
    }

    public Future<Void> delete(Integer id) {
        return dbClient.withTransaction(
                connection -> {
                    return bookRepository.delete(connection, id);
                })
                .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Delete one book", id)))
                .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Delete one book", throwable.getMessage())));
    }

}
