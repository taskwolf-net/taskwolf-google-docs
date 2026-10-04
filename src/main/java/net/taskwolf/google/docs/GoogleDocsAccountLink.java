package net.taskwolf.google.docs;
import net.taskwolf.core.environment.TaskwolfEnvironment;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.common.collect.Lists;
import net.taskwolf.google.GoogleAccountLink;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.account.GoogleUserAccountDatabaseTable;

public final class GoogleDocsAccountLink extends GoogleAccountLink {
  public static GoogleDocsAccountLink create(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable,
    TaskwolfEnvironment environment
  ) {
    return new GoogleDocsAccountLink(googleConfiguration, googleAccountDatabaseTable,
      googleUserAccountDatabaseTable, environment);
  }

  private GoogleDocsAccountLink(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable,
    TaskwolfEnvironment environment
  ) {
    super(googleConfiguration, googleAccountDatabaseTable,
      googleUserAccountDatabaseTable, environment, "google-docs",
      Lists.newArrayList(DocsScopes.DOCUMENTS));
  }
}
