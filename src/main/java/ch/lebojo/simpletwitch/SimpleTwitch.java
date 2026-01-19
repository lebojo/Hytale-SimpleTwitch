package ch.lebojo.simpletwitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.eventsub.condition.ChannelPointsCustomRewardRedemptionAddCondition;
import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionType;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Main plugin class.
 * 
 * TODO: Implement your plugin logic here.
 * 
 * @author LeBojo
 * @version 1.0.2
 */
public class SimpleTwitch extends JavaPlugin {

    private TwitchClient twitchClient;
    public static MyConfig configFile;

    public SimpleTwitch(JavaPluginInit init) {
        super(init);
        setConfigJson();

        System.out.println("[SimpleTwitch] Plugin loaded!");
    }

    @Override
    protected void setup() {
        super.setup();
    }

    @Override
    protected void start() {
        super.start();

        twitchClient = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withEnableEventSocket(true)
                .withDefaultAuthToken(new OAuth2Credential("twitch", configFile.accessToken))
                .build();

        IEventSubSocket eventSocket = twitchClient.getEventSocket();

        String channelId = twitchClient.getHelix()
                .getUsers(configFile.accessToken, null, null)
                .execute()
                .getUsers()
                .get(0)
                .getId();

        twitchClient.getEventManager().onEvent(CustomRewardRedemptionAddEvent.class, event -> {
            String rewardTitle = event.getReward().getTitle();

            if (!rewardTitle.equals(configFile.rewardName)) {
                return;
            }

            String message = event.getUserInput();
            String sender = event.getUserName();

            if (message == null || message.isEmpty()) {
                System.out.println("Message vide de " + sender);
                return;
            }

            List<PlayerRef> onlinePlayers = Universe.get().getPlayers();

            for (PlayerRef player : onlinePlayers) {
                EventTitleUtil.showEventTitleToPlayer(
                        player,
                        Message.raw(message),
                        Message.raw(sender),
                        true
                );
            }
        });

        eventSocket.register(
                (SubscriptionType) SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD,
                (Function) (builder -> ((ChannelPointsCustomRewardRedemptionAddCondition.ChannelPointsCustomRewardRedemptionAddConditionBuilder) builder)
                        .broadcasterUserId(channelId)
                        .build())
        );

        System.out.println("[HytaleTwitch] Plugin enabled!");
    }

    private  void setConfigJson() {
        try {
            Path folderPath = Path.of("mods", "HytaleTwitch");
            Path filePath = folderPath.resolve("config.json");

            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            if (!Files.exists(filePath)) {
                MyConfig defaultConfig = new MyConfig();

                String json = new GsonBuilder().setPrettyPrinting().create().toJson(defaultConfig);
                Files.writeString(filePath, json);
                System.out.println("[HytaleTwitch] Config file created !");
            }

            String content = Files.readString(filePath);
            configFile = new Gson().fromJson(content, MyConfig.class);
        } catch (IOException e) {
            System.err.println("[HytaleTwitch] Config error: " + e.getMessage());
        }
    }
}

