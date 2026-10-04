package net.taskwolf.google.docs.action.create;

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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DocumentCreateAction implements Action<DocumentCreateActionExecutor> {
  public static DocumentCreateAction create(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable,
    InputComponentSelect googleAccountSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("googleAccount", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("documentName", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("documentContent", DatabaseDataType.TEXT));
    return new DocumentCreateAction(googleConfiguration,
      googleAccountDatabaseTable, googleUserAccountDatabaseTable,
      googleAccountSelect,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_google_docs_document_create", contentColumns));
  }

  private final GoogleConfiguration googleConfiguration;
  private final GoogleAccountDatabaseTable googleAccountDatabaseTable;
  private final GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable;
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
  public CompletableFuture<Void> insert(
    UUID actionId, UUID ownerId, Map<String, Object> content
  ) {
    var documentContent = content.get("documentContent");
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(ownerId,
      content.get("googleAccount"), content.get("documentName"),
      documentContent == null ? "" : documentContent));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("googleAccount", row.findCell(2).stringValue(),
        "documentName", row.findCell(3).stringValue(),
        "documentContent", row.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<DocumentCreateActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DocumentCreateActionExecutor.create(googleConfiguration,
        googleAccountDatabaseTable, googleUserAccountDatabaseTable,
        content.findCell(1).uuidValue(), content.findCell(2).stringValue(),
        content.findCell(3).stringValue(), content.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
