package family_fun_pack.commands;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/* A Command */

@OnlyIn(Dist.CLIENT)
public abstract class Command {

  private String name;

  public Command(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  /* When disconnecting from server */
  public void onDisconnect() {}

  public abstract String usage();

  public String getUsage() {
    return "Usage: " + this.usage();
  }

  // Execute a command
  // First argument is always command name
  public abstract String execute(String[] args);

}
