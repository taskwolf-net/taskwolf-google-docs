package com.dulno.google.docs.action.create;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.*;
import com.google.common.collect.Lists;
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
public final class DocumentCreateActionExecutor implements ActionExecutor {
  private final GoogleConfiguration configuration;
  private final GoogleAccountDatabaseTable googleAccountDatabaseTable;
  private final String googleAccount;
  private String documentName;
  private String documentContent;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    documentName = dissolve.dissolve(documentName);
    documentContent = dissolve.dissolve(documentContent);
    var futureResponse = new CompletableFuture<ActionResult>();
    googleAccountDatabaseTable.findAccount(googleAccount).thenAcceptAsync(account ->
      futureResponse.complete(ActionResult.success(buildInformation(
        insertDocument(GoogleCredential.of(configuration.clientId(),
          configuration.clientSecret(), account).buildCredential())))));
    return futureResponse;
  }

  private String insertDocument(Credential credential) {
    try {
      var service = new Docs.Builder(GoogleNetHttpTransport.newTrustedTransport(),
        new GsonFactory(), credential)
        .setApplicationName("Dulno")
        .build();
      var document = new Document();
      document.setTitle(documentName);
      var response = service.documents().create(document).execute();
      if (documentContent != null) {
        var requests = Lists.<Request>newArrayList();
        requests.add(new Request().setInsertText(new InsertTextRequest()
          .setText(documentContent).setLocation(new Location().setIndex(1))));
        var body = new BatchUpdateDocumentRequest().setRequests(requests);
        return service.documents().batchUpdate(response.getDocumentId(), body)
          .execute().getDocumentId();
      }
      return response.getDocumentId();
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
    information.put("documentName", documentName);
    return information;
  }
}