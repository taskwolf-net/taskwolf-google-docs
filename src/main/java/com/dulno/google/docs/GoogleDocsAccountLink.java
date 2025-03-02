package com.dulno.google.docs;
import com.dulno.core.environment.DulnoEnvironment;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.common.collect.Lists;
import com.dulno.google.GoogleAccountLink;
import com.dulno.google.GoogleConfiguration;
import com.dulno.google.account.GoogleAccountDatabaseTable;
import com.dulno.google.account.GoogleUserAccountDatabaseTable;

public final class GoogleDocsAccountLink extends GoogleAccountLink {
  public static GoogleDocsAccountLink create(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable,
    DulnoEnvironment environment
  ) {
    return new GoogleDocsAccountLink(googleConfiguration, googleAccountDatabaseTable,
      googleUserAccountDatabaseTable, environment);
  }

  private GoogleDocsAccountLink(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable,
    DulnoEnvironment environment
  ) {
    super(googleConfiguration, googleAccountDatabaseTable,
      googleUserAccountDatabaseTable, environment, "google-docs",
      Lists.newArrayList(DocsScopes.DOCUMENTS));
  }
}
