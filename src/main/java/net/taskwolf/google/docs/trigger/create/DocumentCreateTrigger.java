package net.taskwolf.google.docs.trigger.create;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerContentDatabaseTable;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DocumentCreateTrigger implements Trigger {
  public static DocumentCreateTrigger create(
    InputComponentSelect googleAccountSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("googleAccount", DatabaseDataType.TEXT));
    return new DocumentCreateTrigger(googleAccountSelect,
      TriggerContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "trigger_google_docs_document_create", contentColumns));
  }

  private final InputComponentSelect googleAccountSelect;
  private final TriggerContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "google-docs-document-create-trigger";
  }

  @Override
  public TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("google.docs.trigger.document.create.name")
      .withDescription("google.docs.trigger.document.create.description")
      .withInputVariable(InputComponentVariable.createSelect("google.docs.trigger.document.create.input.account.name",
        "googleAccount", "google.docs.trigger.document.create.input.account.description", googleAccountSelect))
      .withOutputVariable(OutputComponentVariable.create("google.docs.trigger.document.create.output.document.id", "documentId"))
      .withOutputVariable(OutputComponentVariable.create("google.docs.trigger.document.create.output.document.name", "documentName"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID triggerId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(triggerId,
      DatabaseRow.of(content.get("googleAccount")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("googleAccount", row.findCell(1).stringValue()));
  }

  @Override
  public CompletableFuture<List<UUID>> findEntries(DatabaseCondition condition) {
    return contentDatabaseTable.findContentByCondition(condition).thenApply(
      rows -> rows.stream().map(row -> row.findCell(0).uuidValue()).toList());
  }

  @Override
  public CompletableFuture<Void> delete(UUID triggerId) {
    return contentDatabaseTable.deleteContent(triggerId);
  }
}

