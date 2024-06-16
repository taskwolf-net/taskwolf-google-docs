package net.taskwolf.google.docs.trigger;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.trigger.TriggerEntry;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccount;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.account.GoogleCredential;
import net.taskwolf.google.docs.structure.GoogleDocument;
import net.taskwolf.google.docs.structure.GoogleDocumentDatabaseTable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@RequiredArgsConstructor(staticName = "create")
public final class DocumentCheckSchedule {
  private final CoreModule coreModule;
  private final GoogleConfiguration googleConfiguration;
  private final GoogleAccountDatabaseTable googleAccountDatabaseTable;
  private final GoogleDocumentDatabaseTable googleDocumentDatabaseTable;
  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
  private ScheduledFuture<?> scheduler;

  private static final int INBOX_CHECK_INITIAL_DELAY = 10;
  private static final int INBOX_CHECK_INTERVAL = 5 * 60;
  private static final TimeUnit INBOX_CHECK_TIME_UNIT = TimeUnit.SECONDS;

  public void start() {
    scheduler = executorService.scheduleAtFixedRate(this::execute,
      INBOX_CHECK_INITIAL_DELAY, INBOX_CHECK_INTERVAL, INBOX_CHECK_TIME_UNIT);
  }

  private void execute() {
    coreModule.findAllTriggerEntries("google-docs",
      "google-docs-document-create-trigger").thenAccept(documentCreateTriggers ->
      coreModule.findAllTriggerEntries("google-docs",
        "google-docs-document-delete-trigger").thenAccept(documentDeleteTriggers ->
          assignTriggersToAccounts(documentCreateTriggers, documentDeleteTriggers)
            .thenAccept(this::readDocuments)));
  }

  private CompletableFuture<Multimap<String, TriggerEntry>> assignTriggersToAccounts(
    List<TriggerEntry> documentCreateTriggers, List<TriggerEntry> documentDeleteTriggers
  ) {
    var entries = Lists.<TriggerEntry>newArrayList();
    entries.addAll(documentCreateTriggers);
    entries.addAll(documentDeleteTriggers);
    var futureResponse = new CompletableFuture<Multimap<String, TriggerEntry>>();
    var result = HashMultimap.<String, TriggerEntry>create();
    AsyncIterator.execute(entries, entry ->
      coreModule.findTrigger(entry.module(), entry.type()).get()
        .findContent(entry.id()).thenAccept(content ->
          result.put((String) content.get("googleAccount"), entry)).thenAccept(
          value -> futureResponse.complete(result)));
    return futureResponse;
  }

  private void readDocuments(Multimap<String, TriggerEntry> entries) {
    for (var googleId : entries.keySet()) {
      var accountTriggers = entries.get(googleId);
      googleAccountDatabaseTable.findAccount(googleId)
        .thenApply(this::createDriveService).thenApply(this::listDocsDocuments)
        .thenAccept(currentDocuments -> googleDocumentDatabaseTable
          .findDocuments(googleId).thenAccept(previousDocuments ->
            processDocumentTriggers(googleId, currentDocuments,
              findCreatedDocuments(currentDocuments, previousDocuments),
              findDeletedDocuments(currentDocuments, previousDocuments),
              accountTriggers)));
    }
  }

  private Drive createDriveService(GoogleAccount account) {
    try {
      var credential = GoogleCredential.of(googleConfiguration.clientId(),
        googleConfiguration.clientSecret(), account).buildCredential();
      return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
        new GsonFactory(), credential)
        .setApplicationName("Taskwolf")
        .build();
    } catch (Exception ignored) {
      return null;
    }
  }

  private List<GoogleDocument> listDocsDocuments(Drive service) {
    try {
      return service.files().list().execute().getFiles().stream()
        .filter(file -> file.getMimeType().equals("application/vnd.google-apps.document"))
        .map(file -> GoogleDocument.create(file.getId(), file.getName()))
        .toList();
    } catch (Exception exception) {
      exception.printStackTrace();
      return Lists.newArrayList();
    }
  }

  private List<GoogleDocument> findCreatedDocuments(
    List<GoogleDocument> currentDocuments, List<GoogleDocument> previousDocuments
  ) {
    var createdDocuments = Lists.<GoogleDocument>newArrayList();
    for (var document : currentDocuments) {
      var isCreated = previousDocuments.stream().noneMatch(previousDocument ->
        previousDocument.id().equals(document.id()));
      if (isCreated) {
        createdDocuments.add(document);
      }
    }
    return createdDocuments;
  }

  private List<GoogleDocument> findDeletedDocuments(
    List<GoogleDocument> currentDocuments, List<GoogleDocument> previousDocuments
  ) {
    var deletedDocuments = Lists.<GoogleDocument>newArrayList();
    for (var document : previousDocuments) {
      var isDeleted = currentDocuments.stream().noneMatch(currentDocument ->
        currentDocument.id().equals(document.id()));
      if (isDeleted) {
        deletedDocuments.add(document);
      }
    }
    return deletedDocuments;
  }

  private void processDocumentTriggers(
    String accountId, List<GoogleDocument> currentDocuments,
    List<GoogleDocument> createdDocuments, List<GoogleDocument> deletedDocuments,
    Collection<TriggerEntry> triggers
  ) {
    googleDocumentDatabaseTable.updateDocuments(accountId, currentDocuments);
    for (var entry : triggers) {
      if (entry.type().equals("google-docs-document-create-trigger")) {
        createdDocuments.forEach(document -> executeTrigger(entry.id(), document));
      } else if (entry.type().equals("google-docs-document-delete-trigger")) {
        deletedDocuments.forEach(document -> executeTrigger(entry.id(), document));
      }
    }
  }

  private void executeTrigger(UUID triggerId, GoogleDocument document) {
    coreModule.createWorkflow(triggerId).thenAccept(workflow ->
      workflow.trigger(buildTriggerInformation(document)));
  }

  private Map<String, Object> buildTriggerInformation(GoogleDocument document) {
    var information = Maps.<String, Object>newHashMap();
    information.put("documentId", document.id());
    information.put("documentName", document.name());
    return information;
  }

  public void stop() {
    scheduler.cancel(false);
  }
}

