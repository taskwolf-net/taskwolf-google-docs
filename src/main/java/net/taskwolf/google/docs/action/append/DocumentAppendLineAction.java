package net.taskwolf.google.docs.action.append;

import net.taskwolf.google.account.GoogleUserAccountDatabaseTable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.taskwolf.workflow.action.Action;
import net.taskwolf.workflow.action.ActionContentDatabaseTable;
import net.taskwolf.workflow.action.ActionInformation;
import net.taskwolf.core.database.*;
import net.taskwolf.workflow.component.input.InputComponentDataType;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.docs.action.create.DocumentCreateActionExecutor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DocumentAppendLineAction implements Action<DocumentAppendLineActionExecutor> {
  public static DocumentAppendLineAction create(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable,
    InputComponentSelect googleAccountSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("googleAccount", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("documentId", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("documentLine", DatabaseDataType.TEXT));
    return new DocumentAppendLineAction(googleConfiguration,
      googleAccountDatabaseTable, googleUserAccountDatabaseTable,
      googleAccountSelect,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_google_docs_document_append_line", contentColumns));
  }

  private final GoogleConfiguration googleConfiguration;
  private final GoogleAccountDatabaseTable googleAccountDatabaseTable;
  private final GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable;
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
  public CompletableFuture<Void> insert(
    UUID actionId, UUID ownerId, Map<String, Object> content
  ) {
    var documentLine = content.get("documentLine");
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(ownerId,
      content.get("googleAccount"), content.get("documentId"),
      documentLine == null ? "" : documentLine));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("googleAccount", row.findCell(2).stringValue(),
        "documentId", row.findCell(3).stringValue(),
        "documentLine", row.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<DocumentAppendLineActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DocumentAppendLineActionExecutor.create(googleConfiguration,
        googleAccountDatabaseTable, googleUserAccountDatabaseTable,
        content.findCell(1).uuidValue(), content.findCell(2).stringValue(),
        content.findCell(3).stringValue(), content.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
