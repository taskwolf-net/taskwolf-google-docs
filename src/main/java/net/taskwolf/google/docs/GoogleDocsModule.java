package net.taskwolf.google.docs;

import com.google.inject.Injector;
import net.taskwolf.core.action.ActionRepository;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.core.trigger.TriggerRepository;

@ModuleDescription(name = "google-docs", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class GoogleDocsModule extends Module {
  private Log log;

  public GoogleDocsModule(Injector injector) {
    super(injector.createChildInjector(GoogleDocsInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Google Docs");
  }

  @Override
  public void disable() {

  }

  @Override
  public ModuleInformation moduleInformation() {
    return ModuleInformation.create("Goggle Docs", "", "googledocs.png",
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