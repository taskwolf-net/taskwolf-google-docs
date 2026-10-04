package net.taskwolf.google.docs;

import net.taskwolf.core.environment.TaskwolfEnvironment;
import net.taskwolf.workflow.integration.Integration;
import com.google.inject.Injector;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.workflow.action.ActionRepository;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.google.GoogleAccountLinkRepository;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.account.GoogleUserAccountDatabaseTable;
import net.taskwolf.google.docs.action.append.DocumentAppendLineAction;
import net.taskwolf.google.docs.action.create.DocumentCreateAction;
import net.taskwolf.google.select.GoogleAccountSelect;
import com.google.inject.Key;
import com.google.inject.name.Names;

@ModuleDescription(name = "google-docs", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class GoogleDocsModule extends Integration {
  private Log log;
  private GoogleConfiguration googleConfiguration;
  private GoogleAccountDatabaseTable googleAccountDatabaseTable;
  private GoogleUserAccountDatabaseTable googleUserAccountDatabaseTable;
  private GoogleDocsAccountLink accountLink;
  private InputComponentSelect googleAccountSelect;

  public GoogleDocsModule(Injector injector) {
    super(injector.createChildInjector(GoogleDocsInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Google Docs");
    googleConfiguration = GoogleConfiguration.createAndLoad();
    googleAccountDatabaseTable = injector().getInstance(Key.get(
      GoogleAccountDatabaseTable.class, Names.named("googleDocsAccount")));
    googleUserAccountDatabaseTable = injector().getInstance(Key.get(
      GoogleUserAccountDatabaseTable.class, Names.named("googleDocsUserAccount")));
    accountLink = GoogleDocsAccountLink.create(googleConfiguration,
      googleAccountDatabaseTable, googleUserAccountDatabaseTable,
      injector().getInstance(TaskwolfEnvironment.class));
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
    var repository = ActionRepository.create();
    repository.registerAction(DocumentCreateAction.create(googleConfiguration,
      googleAccountDatabaseTable, googleUserAccountDatabaseTable,
      googleAccountSelect, databaseConnection, databaseKeyspace));
    repository.registerAction(DocumentAppendLineAction.create(googleConfiguration,
      googleAccountDatabaseTable, googleUserAccountDatabaseTable,
      googleAccountSelect, databaseConnection, databaseKeyspace));
    return repository;
  }
}