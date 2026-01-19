package ch.lebojo.simpletwitch

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.eventsub.condition.ChannelPointsCustomRewardRedemptionAddCondition.ChannelPointsCustomRewardRedemptionAddConditionBuilder
import com.github.twitch4j.eventsub.condition.EventSubCondition
import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent
import com.github.twitch4j.eventsub.events.EventSubEvent
import com.github.twitch4j.eventsub.subscriptions.SubscriptionType
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.util.EventTitleUtil
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Function

class SimpleTwitch(init: JavaPluginInit) : JavaPlugin(init) {
    private var twitchClient: TwitchClient? = null

    init {
        setConfigJson()

        println("[SimpleTwitch] Plugin loaded!")
    }

    override fun setup() {
        super.setup()
    }

    override fun start() {
        super.start()

        twitchClient = TwitchClientBuilder.builder()
            .withEnableHelix(true)
            .withEnableEventSocket(true)
            .withDefaultAuthToken(OAuth2Credential("twitch", configFile!!.accessToken))
            .build()

        val eventSocket = twitchClient!!.getEventSocket()

        val channelId = twitchClient!!.getHelix()
            .getUsers(configFile!!.accessToken, null, null)
            .execute()
            .getUsers()
            .get(0)
            .getId()

        twitchClient!!.getEventManager().onEvent<CustomRewardRedemptionAddEvent?>(
            CustomRewardRedemptionAddEvent::class.java,
            Consumer { event: CustomRewardRedemptionAddEvent? ->
                val rewardTitle = event!!.getReward().getTitle()
                if (rewardTitle != configFile!!.rewardName) {
                    return@Consumer
                }

                val message = event.getUserInput()
                val sender = event.getUserName()

                if (message == null || message.isEmpty()) {
                    println("Message vide de " + sender)
                    return@Consumer
                }

                val onlinePlayers = Universe.get().getPlayers()
                for (player in onlinePlayers) {
                    EventTitleUtil.showEventTitleToPlayer(
                        player,
                        Message.raw(message),
                        Message.raw(sender),
                        true
                    )
                }
            })

        eventSocket.register(SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD) { builder ->
            builder.broadcasterUserId(channelId).build()
        }

        println("[HytaleTwitch] Plugin enabled!")
    }

    private fun setConfigJson() {
        try {
            val folderPath = Path.of("mods", "HytaleTwitch")
            val filePath = folderPath.resolve("config.json")

            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath)
            }

            if (!Files.exists(filePath)) {
                val defaultConfig = MyConfig()

                val json = GsonBuilder().setPrettyPrinting().create().toJson(defaultConfig)
                Files.writeString(filePath, json)
                println("[HytaleTwitch] Config file created !")
            }

            val content = Files.readString(filePath)
            configFile = Gson().fromJson<MyConfig>(content, MyConfig::class.java)
        } catch (e: IOException) {
            System.err.println("[HytaleTwitch] Config error: " + e.message)
        }
    }

    companion object {
        var configFile: MyConfig? = null
    }
}

