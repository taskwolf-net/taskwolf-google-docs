package net.taskwolf.google.docs;

import com.google.inject.Injector;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.core.action.ActionRepository;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.core.trigger.TriggerRepository;
import net.taskwolf.google.GoogleAccountLinkRepository;
import net.taskwolf.google.GoogleConfiguration;
import net.taskwolf.google.account.GoogleAccountDatabaseTable;
import net.taskwolf.google.account.GoogleUserAccountDatabaseTable;
import net.taskwolf.google.docs.structure.GoogleDocumentDatabaseTable;

@ModuleDescription(name = "google-docs", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class GoogleDocsModule extends Module {
  private Log log;
  private GoogleDocsAccountLink accountLink;

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
    return TriggerRepository.create();
  }

  @Override
  public ActionRepository actionRepository() {
    return ActionRepository.create();
  }
}