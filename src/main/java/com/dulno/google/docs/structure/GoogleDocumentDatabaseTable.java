package com.dulno.google.docs.structure;

import com.google.common.collect.Lists;
import com.dulno.core.database.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class GoogleDocumentDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "google_document";

  public static GoogleDocumentDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("account", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseListColumn.create("documents", DatabaseDataType.TEXT));
    return new GoogleDocumentDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private GoogleDocumentDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertDocuments(String accountId, List<GoogleDocument> documents) {
    insert(DatabaseRow.of(accountId,
      documents.stream().map(GoogleDocument::toJson).toList()));
  }

  public void updateDocuments(String accountId, List<GoogleDocument> documents) {
    update(accountId, DatabaseRow.of(accountId,
      documents.stream().map(GoogleDocument::toJson).toList()));
  }

  public void deleteDocuments(String accountId) {
    delete(accountId);
  }

  public CompletableFuture<Boolean> documentsExists(String accountId) {
    return exists(accountId);
  }

  public CompletableFuture<List<GoogleDocument>> findDocuments(String accountId) {
    return selectRow(accountId).thenApply(row ->
      row.findCell(1).<String>listValue().stream().map(GoogleDocument::of).toList());
  }
}
