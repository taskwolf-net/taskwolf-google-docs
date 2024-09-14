package com.dulno.google.docs.action.delete;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;
import com.dulno.google.GoogleConfiguration;
import com.dulno.google.account.GoogleAccountDatabaseTable;
import com.dulno.google.account.GoogleCredential;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DocumentDeleteActionExecutor implements ActionExecutor {
  private final GoogleConfiguration configuration;
  private final GoogleAccountDatabaseTable googleAccountDatabaseTable;
  private final String googleAccount;
  private String documentId;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    documentId = dissolve.dissolve(documentId);
    var futureResponse = new CompletableFuture<ActionResult>();
    googleAccountDatabaseTable.findAccount(googleAccount)
      .thenAcceptAsync(account -> deleteDocument(GoogleCredential.of(configuration.clientId(),
        configuration.clientSecret(), account).buildCredential()))
      .thenAccept(value -> futureResponse.complete(ActionResult.success(
        buildInformation())));
    return futureResponse;
  }

  private void deleteDocument(Credential credential) {
    try {
      var service = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
        new GsonFactory(), credential)
        .setApplicationName("Dulno")
        .build();
      service.files().delete(documentId).execute();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private Map<String, Object> buildInformation() {
    var information = Maps.<String, Object>newHashMap();
    information.put("documentId", documentId);
    return information;
  }
}