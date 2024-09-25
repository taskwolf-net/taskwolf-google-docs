package com.dulno.google.docs;

import com.google.inject.Injector;
import com.dulno.core.account.AccountLink;
import com.dulno.core.action.ActionRepository;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.log.Log;
import com.dulno.core.module.Module;
import com.dulno.core.module.ModuleDescription;
import com.dulno.core.module.ModuleInformation;
import com.dulno.core.module.ModuleLoadPriority;
import com.dulno.core.workflow.component.input.InputComponentSelect;
import com.dulno.google.GoogleAccountLinkRepository;
import com.dulno.google.GoogleConfiguration;
import com.dulno.google.account.GoogleAccountDatabaseTable;
import com.dulno.google.account.GoogleUserAccountDatabaseTable;
import com.dulno.google.docs.action.append.DocumentAppendLineAction;
import com.dulno.google.docs.action.create.DocumentCreateAction;
import com.dulno.google.select.GoogleAccountSelect;

@ModuleDescription(name = "google-docs", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class GoogleDocsModule extends Module {
  private Log log;
  private GoogleDocsAccountLink accountLink;
  private InputComponentSelect googleAccountSelect;

  public GoogleDocsModule(Injector injector) {
    super(injector.createChildInjector(GoogleDocsInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Google Docs");
    var googleAccountDatabaseTable = injector().getInstance(
      GoogleAccountDatabaseTable.class);
    var googleUserAccountDatabaseTable = injector().getInstance(
      GoogleUserAccountDatabaseTable.class);
    accountLink = GoogleDocsAccountLink.create(
      injector().getInstance(GoogleConfiguration.class),
      googleAccountDatabaseTable, googleUserAccountDatabaseTable);
    injector().getInstance(GoogleAccountLinkRepository.class)
      .registerGoogleAccountLink(accountLink);
    googleAccountSelect = GoogleAccountSelect.create(googleAccountDatabaseTable,
      googleUserAccountDatabaseTable);
    new java.io.File(System.getProperty("user.dir") +
      "/files/").mkdirs();
  }

  @Override
  public void disable() {

  }

  @Override
  public AccountLink accountLink() {
    return accountLink;
  }

  @Override
  public ModuleInformation moduleInformation() {
    return ModuleInformation.create("Google Docs", "", "googledocs.png",
      ModuleInformation.Type.PUBLIC);
  }

  @Override
  public ActionRepository actionRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var googleConfiguration = injector().getInstance(GoogleConfiguration.class);
    var accountDatabaseTable = injector().getInstance(GoogleAccountDatabaseTable.class);
    var repository = ActionRepository.create();
    repository.registerAction(DocumentCreateAction.create(googleConfiguration,
      accountDatabaseTable, googleAccountSelect, databaseConnection,
      databaseKeyspace));
    repository.registerAction(DocumentAppendLineAction.create(googleConfiguration,
      accountDatabaseTable, googleAccountSelect, databaseConnection,
      databaseKeyspace));
    return repository;
  }
}