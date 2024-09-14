package com.dulno.google.docs.action.create;

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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DocumentCreateAction implements Action<DocumentCreateActionExecutor> {
  public static DocumentCreateAction create(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    InputComponentSelect googleAccountSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("googleAccount", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("documentName", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("documentContent", DatabaseDataType.TEXT));
    return new DocumentCreateAction(googleConfiguration, googleAccountDatabaseTable,
      googleAccountSelect, ActionContentDatabaseTable.create(databaseConnection,
      databaseKeyspace, "action_google_docs_document_create", contentColumns));
  }

  private final GoogleConfiguration googleConfiguration;
  private final GoogleAccountDatabaseTable googleAccountDatabaseTable;
  private final InputComponentSelect googleAccountSelect;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "google-docs-document-create-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("google.docs.action.document.create.name")
      .withDescription("google.docs.action.document.create.description")
      .withInputVariable(InputComponentVariable.createSelect("google.docs.action.document.create.input.account.name",
        "googleAccount", "google.docs.action.document.create.input.account.description", googleAccountSelect))
      .withInputVariable(InputComponentVariable.createRequired("google.docs.action.document.create.input.document.name.name",
        "documentName", "google.docs.action.document.create.input.document.name.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("google.docs.action.document.create.input.document.content.name",
        "documentContent", "google.docs.action.document.create.input.document.content.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("google.docs.action.document.create.output.document.id", "documentId"))
      .withOutputVariable(OutputComponentVariable.create("google.docs.action.document.create.output.document.name", "documentName"))
      .withOutputVariable(OutputComponentVariable.create("google.docs.action.document.create.output.document.content", "documentContent"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("googleAccount"), content.get("documentName"),
      content.get("documentContent")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("googleAccount", row.findCell(1).stringValue(), "documentName",
        row.findCell(2).stringValue(), "documentContent",
        row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<DocumentCreateActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DocumentCreateActionExecutor.create(googleConfiguration,
        googleAccountDatabaseTable, content.findCell(1).stringValue(),
        content.findCell(2).stringValue(), content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
