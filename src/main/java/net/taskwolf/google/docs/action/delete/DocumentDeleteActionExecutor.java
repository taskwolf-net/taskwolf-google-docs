package net.taskwolf.google.docs.action.delete;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.account.GoogleCredential;

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
      .thenAccept(account -> deleteDocument(GoogleCredential.of(configuration.clientId(),
        configuration.clientSecret(), account).buildCredential()))
      .thenAccept(value -> futureResponse.complete(ActionResult.success(
        buildInformation())));
    return futureResponse;
  }

  private void deleteDocument(Credential credential) {
    try {
      var service = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
        new GsonFactory(), credential)
        .setApplicationName("Taskwolf")
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