package net.taskwolf.google.docs.action.upload;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.account.GoogleCredential;
import org.apache.commons.codec.binary.Base64;

import java.io.FileOutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DocumentUploadActionExecutor implements ActionExecutor {
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
    googleAccountDatabaseTable.findAccount(googleAccount).thenAccept(account ->
      futureResponse.complete(ActionResult.success(buildInformation(
        uploadFile(GoogleCredential.of(configuration.clientId(),
          configuration.clientSecret(), account).buildCredential())))));
    return futureResponse;
  }

  private File uploadFile(Credential credential) {
    try {
      var service = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
        new GsonFactory(), credential)
        .setApplicationName("Taskwolf")
        .build();
      var file = new File();
      //TODO: SET FILE TYPE GOOGLE DOCS DOC
      file.setName(documentName);
      var filePath = generateAvailableFilePath();
      var data = Base64.decodeBase64(documentContent);
      try (var stream = new FileOutputStream(filePath)) {
        stream.write(data);
      }
      var temporaryFile = new java.io.File(filePath);
      var fileContent = new FileContent("", temporaryFile);
      var result = service.files().create(file, fileContent).setFields("id").execute();
      temporaryFile.delete();
      return result;
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private String generateAvailableFilePath() {
    var filePath = System.getProperty("user.dir") +
      "/files/" + UUID.randomUUID().toString();
    var file = new java.io.File(filePath);
    if (file.exists()) {
      return generateAvailableFilePath();
    }
    return filePath;
  }

  private Map<String, Object> buildInformation(File file) {
    if (file == null) {
      return Maps.newHashMap();
    }
    var information = Maps.<String, Object>newHashMap();
    information.put("documentId", file.getId());
    information.put("documentName", documentName);
    information.put("documentContent", documentContent);
    return information;
  }
}
