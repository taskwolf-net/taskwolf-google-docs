package net.taskwolf.google.docs.action.delete;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionContentDatabaseTable;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.database.*;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.docs.action.create.DocumentCreateActionExecutor;

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

