package com.dulno.google.docs;
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
    GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable
  ) {
    return new GoogleDocsAccountLink(googleConfiguration, googleAccountDatabaseTable,
      googleUserAccountDatabaseTable);
  }

  private GoogleDocsAccountLink(
    GoogleConfiguration googleConfiguration,
    GoogleAccountDatabaseTable googleAccountDatabaseTable,
    GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable
  ) {
    super(googleConfiguration, googleAccountDatabaseTable,
      googleUserAccountDatabaseTable, "google-docs",
      Lists.newArrayList(DocsScopes.DOCUMENTS));
  }
}
