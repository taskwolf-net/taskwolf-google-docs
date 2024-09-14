package com.dulno.google.docs.action.delete;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import com.dulno.core.action.Action;
import com.dulno.core.action.ActionContentDatabaseTable;
import com.dulno.core.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.core.workflow.component.input.InputComponentDataType;
import com.dulno.core.workflow.component.input.InputComponentSelect;
import com.dulno.core.workflow.component.input.InputComponentVariable;
import com.dulno.core.workflow.component.output.OutputComponentVariable;
import com.dulno.google.GoogleConfiguration;
import com.dulno.google.account.GoogleAccountDatabaseTable;
import com.dulno.google.docs.action.create.DocumentCreateActionExecutor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DocumentDeleteAction implements Action<DocumentDeleteActionExecutor> {
  public static DocumentDeleteAction create(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    InputComponentSelect googleAccountSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("googleAccount", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("documentId", DatabaseDataType.TEXT));
    return new DocumentDeleteAction(googleConfiguration, googleAccountDatabaseTable,
      googleAccountSelect, ActionContentDatabaseTable.create(databaseConnection,
      databaseKeyspace, "action_google_docs_document_delete", contentColumns));
  }

  private final GoogleConfiguration googleConfiguration;
  private final GoogleAccountDatabaseTable googleAccountDatabaseTable;
  private final InputComponentSelect googleAccountSelect;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "google-docs-document-delete-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("google.docs.action.document.delete.name")
      .withDescription("google.docs.action.document.delete.description")
      .withInputVariable(InputComponentVariable.createSelect("google.docs.action.document.delete.input.account.name",
        "googleAccount", "google.docs.action.document.delete.input.account.description", googleAccountSelect))
      .withInputVariable(InputComponentVariable.createRequired("google.docs.action.document.delete.input.document.id.name",
        "documentId", "google.docs.action.document.delete.input.document.id.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("google.drive.action.document.delete.output.document.id", "documentId"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("googleAccount"), content.get("documentId")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("googleAccount", row.findCell(1).stringValue(), "documentId",
        row.findCell(2).stringValue()));
  }

  @Override
  public CompletableFuture<DocumentDeleteActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DocumentDeleteActionExecutor.create(googleConfiguration,
        googleAccountDatabaseTable, content.findCell(1).stringValue(),
        content.findCell(2).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}

