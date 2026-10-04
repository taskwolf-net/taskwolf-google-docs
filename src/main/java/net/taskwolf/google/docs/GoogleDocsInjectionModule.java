package net.taskwolf.google.docs;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.account.GoogleUserAccountDatabaseTable;

@RequiredArgsConstructor(staticName = "create")
public final class GoogleDocsInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  @Named("googleDocsAccount")
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
  @Named("googleDocsUserAccount")
  GoogleUserAccountDatabaseTable provideGoogleUserAccountDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var googleUserAccountDatabaseTable = GoogleUserAccountDatabaseTable.create(
      connection, keyspace, "google_docs_user_account");
    googleUserAccountDatabaseTable.createIfNotExists();
    googleUserAccountDatabaseTable.createIndexIfNotExists("accounts");
    return googleUserAccountDatabaseTable;
  }
}