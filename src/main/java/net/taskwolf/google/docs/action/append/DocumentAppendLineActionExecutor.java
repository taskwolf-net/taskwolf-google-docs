package net.taskwolf.google.docs.action.append;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.*;
import com.google.common.collect.Lists;
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
public final class DocumentAppendLineActionExecutor implements ActionExecutor {
  private final GoogleConfiguration configuration;
  private final GoogleAccountDatabaseTable googleAccountDatabaseTable;
  private final String googleAccount;
  private String documentId;
  private String documentLine;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    documentId = dissolve.dissolve(documentId);
    documentLine = dissolve.dissolve(documentLine);
    var futureResponse = new CompletableFuture<ActionResult>();
    googleAccountDatabaseTable.findAccount(googleAccount).thenAccept(account ->
      futureResponse.complete(ActionResult.success(buildInformation(
        appendLineToDocument(GoogleCredential.of(configuration.clientId(),
          configuration.clientSecret(), account).buildCredential())))));
    return futureResponse;
  }

  private String appendLineToDocument(Credential credential) {
    try {
      var service = new Docs.Builder(GoogleNetHttpTransport.newTrustedTransport(),
        new GsonFactory(), credential)
        .setApplicationName("Taskwolf")
        .build();
      var content = service.documents().get(documentId).execute().getBody()
        .getContent();
      var index = content.get(content.size() - 1).getEndIndex() - 1;
      var requests = Lists.<Request>newArrayList();
      requests.add(new Request().setInsertText(new InsertTextRequest()
        .setText("\n" + documentLine).setLocation(new Location().setIndex(index))));
      var body = new BatchUpdateDocumentRequest().setRequests(requests);
      return service.documents().batchUpdate(documentId, body)
        .execute().getDocumentId();
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private Map<String, Object> buildInformation(String documentId) {
    if (documentId == null) {
      return Maps.newHashMap();
    }
    var information = Maps.<String, Object>newHashMap();
    information.put("documentId", documentId);
    information.put("documentLine", documentLine);
    return information;
  }
}
