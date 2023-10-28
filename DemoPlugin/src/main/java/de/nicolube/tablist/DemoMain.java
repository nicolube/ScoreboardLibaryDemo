package de.nicolube.tablist;

import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import org.bukkit.plugin.java.JavaPlugin;

public class DemoMain extends JavaPlugin {


@Override
    public void onEnable() {

    ScoreboardLibrary scoreboardLibrary;
    try {
        scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(this);
    } catch (NoPacketAdapterAvailableException e) {
        scoreboardLibrary = new NoopScoreboardLibrary();
    }

    getServer().getPluginManager().registerEvents(new TabListListener(scoreboardLibrary.createTeamManager()), this);
}

}
