package world.icebear03.splendidenchants.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.SimpleCommandBody
import taboolib.common.platform.command.subCommand
import taboolib.module.kether.isInt
import taboolib.platform.util.isAir
import world.icebear03.splendidenchants.api.EnchantAPI
import world.icebear03.splendidenchants.api.ItemAPI
import world.icebear03.splendidenchants.command.CommandExecutor
import world.icebear03.splendidenchants.command.CommandHandler
import world.icebear03.splendidenchants.enchant.EnchantLoader
import world.icebear03.splendidenchants.enchant.SplendidEnchant

/**
 * SplendidEnchants
 * world.icebear03.splendidenchants.command.sub.CommandEnchant
 *
 * @author Mical
 * @since 2023/7/12 00:41
 */
object CommandEnchant : CommandExecutor {

    //FIXME 貌似不太好简化……

    override val command: SimpleCommandBody = subCommand {
        dynamic("enchant") {
            suggestionUncheck<Player> { _, _ ->
                // FIXME
                EnchantLoader.enchantById.keys().toList().toMutableList().also {
                    it.addAll(EnchantLoader.enchantByName.keys().toList())
                }
            }
            execute<Player> { sender, ctx, _ ->
                val enchant = EnchantAPI.getSplendidEnchant(ctx["enchant"])

                if (enchant == null) {
                    sender.sendMessage("§6SplendidEnchants §7>> §7附魔 §f${ctx["enchant"]} §7不存在.")
                    return@execute
                }
                enchant(sender, enchant)
            }

            dynamic("level") {
                suggestionUncheck<Player> { _, ctx ->
                    val enchant =
                        EnchantAPI.getSplendidEnchant(ctx["enchant"]) ?: return@suggestionUncheck (0..9).toList()
                            .map { it.toString() }
                    return@suggestionUncheck (0..enchant.maxLevel).toList().map { it.toString() }
                }

                execute<Player> { sender, ctx, _ ->
                    val enchant = EnchantAPI.getSplendidEnchant(ctx["enchant"])

                    if (enchant == null) {
                        sender.sendMessage("§6SplendidEnchants §7>> §7附魔 §f${ctx["enchant"]} §7不存在.")
                        return@execute
                    }
                    val level = ctx["level"]
                    if (!level.isInt()) {
                        sender.sendMessage("§6SplendidEnchants §7>> §7等级必须为数字.")
                        return@execute
                    }
                    if (level.toInt() < 0) {
                        sender.sendMessage("§6SplendidEnchants §7>> §7等级必须大于等于0(0为清除附魔).")
                        return@execute
                    }
                    enchant(sender, enchant, level.toInt())
                }
            }
        }
    }

    fun enchant(player: Player, enchant: SplendidEnchant, level: Int = enchant.maxLevel) {
        val item = player.inventory.itemInMainHand
        if (item.isAir) {
            player.sendMessage("§6SplendidEnchants §7>> §7你必须手持物品才可以对物品附魔.")
            return
        }

        when {
            level > 0 -> {
                ItemAPI.addEnchant(item, enchant, level.toInt())
                player.sendMessage("§6SplendidEnchants §7>> §7已成功为物品添加附魔 ${enchant.rarity.color}${enchant.basicData.name}§7, 等级 §f$level§7.")
            }

            level == 0 -> {
                ItemAPI.removeEnchant(item, enchant)
                player.sendMessage("§6SplendidEnchants §7>> §7已成功为物品清除附魔 ${enchant.rarity.color}${enchant.basicData.name}§7.")
            }
        }
    }

    override val name: String
        get() = "enchant"

    override val description: String
        get() = "为手中物品附魔"

    override val usage: String
        get() = "§e<附魔ID/名称> §6[附魔等级]"

    init {
        CommandHandler.sub[name] = this
    }
}