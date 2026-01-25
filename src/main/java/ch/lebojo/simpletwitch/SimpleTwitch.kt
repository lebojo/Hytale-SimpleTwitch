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

        if (configFile?.accessTokens.isNullOrEmpty()) {
            println("[HytaleTwitch] Pas de tokens configurés !")
            return
        }

        twitchClient = TwitchClientBuilder.builder()
            .withEnableHelix(true)
            .withEnableEventSocket(true)
            .withDefaultAuthToken(OAuth2Credential("twitch", configFile!!.accessTokens[0]))
            .build()

        val eventSocket = twitchClient!!.eventSocket

        twitchClient!!.eventManager.onEvent<CustomRewardRedemptionAddEvent?>(
            CustomRewardRedemptionAddEvent::class.java,
            Consumer { event: CustomRewardRedemptionAddEvent? ->
                val rewardTitle = event?.reward?.title ?: return@Consumer
                if (rewardTitle != configFile!!.rewardName) return@Consumer

                val message = event.userInput
                val sender = event.userName

                if (message.isNullOrEmpty()) return@Consumer

                Universe.get().players.forEach { player ->
                    EventTitleUtil.showEventTitleToPlayer(
                        player,
                        Message.raw(message),
                        Message.raw(sender),
                        true
                    )
                }
            })

        configFile!!.accessTokens.forEach { token ->
            try {
                val user = twitchClient!!.helix
                    .getUsers(token, null, null)
                    .execute()
                    .users
                    .firstOrNull()

                if (user != null) {
                    eventSocket.register(SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD) { builder ->
                        builder.broadcasterUserId(user.id).build()
                    }
                    println("[HytaleTwitch] Listen active pour : ${user.displayName}")
                }
            } catch (e: Exception) {
                System.err.println("[HytaleTwitch] Error token : ${e.message}")
            }
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

