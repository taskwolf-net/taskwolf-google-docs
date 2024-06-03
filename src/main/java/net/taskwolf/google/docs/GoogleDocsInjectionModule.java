package net.taskwolf.google.docs;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.account.GoogleUserAccountDatabaseTable;
import net.taskwolf.google.docs.structure.GoogleDocumentDatabaseTable;

@RequiredArgsConstructor(staticName = "create")
public final class GoogleDocsInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  GoogleConfiguration provideGoogleConfiguration() throws Exception {
    return GoogleConfiguration.createAndLoad();
  }

  @Provides
  @Singleton
  GoogleAccountDatabaseTable provideGoogleAccountDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var googleAccountDatabaseTable = GoogleAccountDatabaseTable.create(
      connection, keyspace, "google_docs_account");
    googleAccountDatabaseTable.createIfNotExists();
    return googleAccountDatabaseTable;
  }

  @Provides
  @Singleton
  GoogleUserAccountDatabaseTable provideGoogleUserAccountDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var googleUserAccountDatabaseTable = GoogleUserAccountDatabaseTable.create(
      connection, keyspace, "google_docs_user_account");
    googleUserAccountDatabaseTable.createIfNotExists();
    googleUserAccountDatabaseTable.createIndexIfNotExists("accounts");
    return googleUserAccountDatabaseTable;
  }

  @Provides
  @Singleton
  GoogleDocumentDatabaseTable provideGoogleDocumentDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var googleDocumentDatabaseTable = GoogleDocumentDatabaseTable.create(
      connection, keyspace);
    googleDocumentDatabaseTable.createIfNotExists();
    return googleDocumentDatabaseTable;
  }
}