package net.taskwolf.google.docs.action.create;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.Document;
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
public final class DocumentCreateActionExecutor implements ActionExecutor {
  private final GoogleConfiguration configuration;
  private final GoogleAccountDatabaseTable googleAccountDatabaseTable;
  private final String googleAccount;
  private String documentName;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    documentName = dissolve.dissolve(documentName);
    var futureResponse = new CompletableFuture<ActionResult>();
    googleAccountDatabaseTable.findAccount(googleAccount).thenAccept(account ->
      futureResponse.complete(ActionResult.success(buildInformation(
        insertDocument(GoogleCredential.of(configuration.clientId(),
          configuration.clientSecret(), account).buildCredential())))));
    return futureResponse;
  }

  private Document insertDocument(Credential credential) {
    try {
      var service = new Docs.Builder(GoogleNetHttpTransport.newTrustedTransport(),
        new GsonFactory(), credential)
        .setApplicationName("Taskwolf")
        .build();
      var document = new Document();
      document.setTitle(documentName);
      return service.documents().create(document).setFields("id").execute();
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private Map<String, Object> buildInformation(Document document) {
    if (document == null) {
      return Maps.newHashMap();
    }
    var information = Maps.<String, Object>newHashMap();
    information.put("documentId", document.getDocumentId());
    information.put("documentName", documentName);
    return information;
  }
}