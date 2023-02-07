package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot
import com.learnspigot.bot.LearnSpigotBot.Companion.editEmbed
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import java.awt.Color

class EmbedCommand(guild: Guild, private val bot: JDA) {
    init {
        guild.upsertCommand("embed", "Post a custom embed to a channel") {
            restrict(true, Permission.MANAGE_SERVER)
            option<TextChannel>("channel", "The channel you would like to send the embed in", true)
            option<String>("title", "The title of the embed", true)
            option<String>("color", "The color of the embed", false)
            bot.onCommand("embed") {
                it.replyModal("embed-" +
                        "${it.getOption("channel")!!.asChannel.id}-" +
                        it.getOption("title")!!.asString +
                        if (it.getOption("color") != null) {
                            "-${it.getOption("color")!!.asString}"
                        } else {
                            "-${LearnSpigotBot.EMBED_COLOR}"
                        }, "Create Embed") {
                    paragraph("description", "The description", true)
                    paragraph("footer", "The footer", false)
                    paragraph("thumbnail", "The thumbnail", false)
                    paragraph("image", "The image", false)
                    paragraph("author", "The author", false)
                }.queue()
            }
            bot.listener<ModalInteractionEvent> {modalEvent ->
                if(!modalEvent.modalId.startsWith("embed-")) return@listener
                modalEvent.deferReply(true).queue()
                val channel = modalEvent.guild!!.getTextChannelById(modalEvent.modalId.split(Regex.fromLiteral("-"))[1])!!
                val embedTitle = modalEvent.modalId.split(Regex.fromLiteral("-"))[2]
                val embedColor = modalEvent.modalId.split(Regex.fromLiteral("-"))[3]

                val embedDescription = modalEvent.getValue("description")!!.asString
                val embedFooter = modalEvent.getValue("footer")!!.asString
                val embedThumbnail = modalEvent.getValue("thumbnail")!!.asString
                val embedImage = modalEvent.getValue("image")!!.asString
                val embedAuthor = modalEvent.getValue("author")!!.asString

                channel.sendMessageEmbeds(Embed {
                    title = embedTitle
                    description = embedDescription
                    if(embedFooter != "") {
                        footer(embedFooter)
                    }
                    if(embedThumbnail != "") {
                        thumbnail = embedThumbnail
                    }
                    if(embedImage != "") {
                        image = embedImage
                    }
                    if(embedAuthor != "") {
                        author(embedAuthor)
                    }
                    color = Color.decode(embedColor).rgb
                }).complete()
                modalEvent.editEmbed({
                    title = "Success"
                    description = "Embed sent to ${channel.asMention}"
                })
            }
        }.queue()
    }
}