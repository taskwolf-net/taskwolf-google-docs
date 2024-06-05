package net.taskwolf.google.docs;

import com.google.inject.Injector;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.core.action.ActionRepository;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.core.trigger.TriggerRepository;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.google.GoogleAccountLinkRepository;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.account.GoogleUserAccountDatabaseTable;
import net.taskwolf.google.docs.action.create.DocumentCreateAction;
import net.taskwolf.google.docs.action.delete.DocumentDeleteAction;
import net.taskwolf.google.docs.structure.GoogleDocumentDatabaseTable;
import net.taskwolf.google.docs.trigger.create.DocumentCreateTrigger;
import net.taskwolf.google.docs.trigger.delete.DocumentDeleteTrigger;
import net.taskwolf.google.select.GoogleAccountSelect;

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
      googleAccountDatabaseTable, googleUserAccountDatabaseTable,
      injector().getInstance(GoogleDocumentDatabaseTable.class));
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
  public TriggerRepository triggerRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var repository = TriggerRepository.create();
    repository.registerTrigger(DocumentCreateTrigger.create(googleAccountSelect,
      databaseConnection, databaseKeyspace));
    repository.registerTrigger(DocumentDeleteTrigger.create(googleAccountSelect,
      databaseConnection, databaseKeyspace));
    return repository;
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
    repository.registerAction(DocumentDeleteAction.create(googleConfiguration,
      accountDatabaseTable, googleAccountSelect, databaseConnection,
      databaseKeyspace));
    return repository;
  }
}