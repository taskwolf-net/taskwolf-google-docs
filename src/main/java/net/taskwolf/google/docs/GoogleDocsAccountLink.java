package net.taskwolf.google.docs;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.common.collect.Lists;
import net.taskwolf.google.GoogleAccountLink;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccount;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.account.GoogleCredential;
import net.taskwolf.google.account.GoogleUserAccountDatabaseTable;
import net.taskwolf.google.docs.structure.GoogleDocument;
import net.taskwolf.google.docs.structure.GoogleDocumentDatabaseTable;

import java.util.List;
import java.util.UUID;

public final class GoogleDocsAccountLink extends GoogleAccountLink {
  public static GoogleDocsAccountLink create(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable,
    GoogleDocumentDatabaseTable googleDocumentDatabaseTable
  ) {
    return new GoogleDocsAccountLink(googleConfiguration, googleAccountDatabaseTable,
      googleUserAccountDatabaseTable, googleDocumentDatabaseTable);
  }

  private final GoogleDocumentDatabaseTable googleDocumentDatabaseTable;

  private GoogleDocsAccountLink(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable,
    GoogleDocumentDatabaseTable googleDocumentDatabaseTable
  ) {
    super(googleConfiguration, googleAccountDatabaseTable,
      googleUserAccountDatabaseTable, "google-docs",
      Lists.newArrayList(DocsScopes.DOCUMENTS, DriveScopes.DRIVE));
    this.googleDocumentDatabaseTable = googleDocumentDatabaseTable;
  }

  @Override
  public void registerAccount(UUID id, String identifier) throws Exception {
    super.registerAccount(id, identifier);
    googleAccountDatabaseTable.findAccount(identifier)
      .thenApply(this::createDriveService).thenApply(this::findDocsDocuments)
      .thenAccept(documents -> googleDocumentDatabaseTable.insertDocuments(
        identifier, documents));
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

  private List<GoogleDocument> findDocsDocuments(Drive service) {
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

  @Override
  public void removeAccount(UUID id, String identifier) {
    super.removeAccount(id, identifier);
    googleDocumentDatabaseTable.deleteDocuments(identifier);
  }
}
