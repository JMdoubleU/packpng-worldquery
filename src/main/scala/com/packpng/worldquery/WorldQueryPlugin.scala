package com.packpng.worldquery

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import com.packpng.worldquery.monitor.WorldAreaPublisher
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class WorldQueryPlugin extends JavaPlugin {

  private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

  override def onEnable(): Unit = {
    this.saveDefaultConfig()
    val config: FileConfiguration = this.getConfig

    val publisher = new WorldAreaPublisher(this, config)

    val updateIntervalSeconds = this.getConfig.getInt("output.updateIntervalSeconds")
    executor.scheduleAtFixedRate(publisher, 0, updateIntervalSeconds, TimeUnit.SECONDS)
  }

  override def onDisable(): Unit = {
    executor.shutdownNow()
    executor.awaitTermination(5, TimeUnit.SECONDS)
  }
}
