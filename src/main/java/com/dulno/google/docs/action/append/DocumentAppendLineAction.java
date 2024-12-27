package com.dulno.google.docs.action.append;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import com.dulno.workflow.action.Action;
import com.dulno.workflow.action.ActionContentDatabaseTable;
import com.dulno.workflow.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.workflow.component.input.InputComponentDataType;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
import com.dulno.google.GoogleConfiguration;
import com.dulno.google.account.GoogleAccountDatabaseTable;
import com.dulno.google.docs.action.create.DocumentCreateActionExecutor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DocumentAppendLineAction implements Action<DocumentAppendLineActionExecutor> {
  public static DocumentAppendLineAction create(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    InputComponentSelect googleAccountSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("googleAccount", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("documentId", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("documentLine", DatabaseDataType.TEXT));
    return new DocumentAppendLineAction(googleConfiguration, googleAccountDatabaseTable,
      googleAccountSelect, ActionContentDatabaseTable.create(databaseConnection,
      databaseKeyspace, "action_google_docs_document_append_line", contentColumns));
  }

  private final GoogleConfiguration googleConfiguration;
  private final GoogleAccountDatabaseTable googleAccountDatabaseTable;
  private final InputComponentSelect googleAccountSelect;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "google-docs-document-append-line-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("google.docs.action.document.append.line.name")
      .withDescription("google.docs.action.document.append.line.description")
      .withInputVariable(InputComponentVariable.createSelect("google.docs.action.document.append.line.input.account.name",
        "googleAccount", "google.docs.action.document.append.line.input.account.description", googleAccountSelect))
      .withInputVariable(InputComponentVariable.createRequired("google.docs.action.document.append.line.input.document.id.name",
        "documentId", "google.docs.action.document.append.line.input.document.id.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("google.docs.action.document.append.line.input.document.line.name",
        "documentLine", "google.docs.action.document.append.line.input.document.line.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("google.docs.action.document.append.line.output.document.id", "documentId"))
      .withOutputVariable(OutputComponentVariable.create("google.docs.action.document.append.line.output.document.line", "documentLine"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("googleAccount"), content.get("documentId"),
      content.get("documentLine")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("googleAccount", row.findCell(1).stringValue(), "documentId",
        row.findCell(2).stringValue(), "documentLine",
        row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<DocumentAppendLineActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DocumentAppendLineActionExecutor.create(googleConfiguration,
        googleAccountDatabaseTable, content.findCell(1).stringValue(),
        content.findCell(2).stringValue(), content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
