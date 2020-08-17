package com.packpng.worldquery.monitor

import java.util.{Observable, Observer}

import better.files.File
import com.packpng.worldquery.monitor.WorldAreaPublisher._
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.{BoundingBox, Vector => BVector}

import scala.jdk.CollectionConverters._

class WorldAreaPublisher(
  plugin: JavaPlugin,
  config: FileConfiguration
) extends Runnable with Observer {

  private val origin: BVector = new BVector(
    config.getInt("area.origin.x"),
    config.getInt("area.origin.y"),
    config.getInt("area.origin.z"),
  )

  private val listener: WorldAreaListener = {
    val world = plugin.getServer.getWorld(config.getString("area.world"))

    val radiusX = config.getInt("area.radiusSize.x")
    val radiusY = config.getInt("area.radiusSize.y")
    val radiusZ = config.getInt("area.radiusSize.z")

    val boundingBox = BoundingBox.of(
      new BVector(
        origin.getBlockX - radiusX,
        origin.getBlockY - radiusY,
        origin.getBlockZ - radiusZ,
      ),
      new BVector(
        origin.getBlockX + radiusX,
        origin.getBlockY + radiusY,
        origin.getBlockZ + radiusZ,
      )
    )

    val materials = config.getStringList("area.materials").asScala.toSet

    val listener = new WorldAreaListener(world, boundingBox, materials)
    listener.addObserver(this)
    plugin.getServer.getPluginManager.registerEvents(listener, plugin)
    listener
  }

  private val outputSettings: OutputSettings = OutputSettings(
    file = File(config.getString("output.directory")) / config.getString("output.filename"),
    archiveDirectory = if (config.getBoolean("output.archive.enabled")) {
      Some(File(config.getString("output.archive.directory")))
    } else None,
    updateIntervalSeconds = config.getInt("output.updateIntervalSeconds")
  )

  private var changed = true

  override def run(): Unit = this.synchronized {
    if (changed) {
      outputSettings.archiveDirectory match {
        case Some(archiveDirectory) =>
          archiveFile(outputSettings.file, archiveDirectory)
        case _ => () // Archiving disabled
      }

      val outputLines = listener.getBlocks.map{ case (location, material) =>
        // Change block location to be relative to configured origin
        val adjustedLocation = adjustVectorOrigin(origin, location.toVector)

        s"${adjustedLocation.getBlockX},${adjustedLocation.getBlockY},${adjustedLocation.getBlockZ},$material"
      }
      outputSettings.file.clear()
      outputSettings.file.printLines(outputLines)
      changed = false
    }
  }

  override def update(o: Observable, arg: Any): Unit = this.synchronized {
    changed = true
  }
}

object WorldAreaPublisher {

  private case class OutputSettings(
    file: File,
    archiveDirectory: Option[File],
    updateIntervalSeconds: Int
  )

  private def adjustVectorOrigin(origin: BVector, point: BVector): BVector =
    point.subtract(origin)

  private def archiveFile(file: File, archiveDirectory: File): Unit = {
    archiveDirectory.createDirectoryIfNotExists(createParents=true)

    if (file.exists) {
      file.copyTo(archiveDirectory / getArchiveFilename(file), overwrite=true)
    }
  }

  private def getArchiveFilename(file: File): String =
    file.name + "_" + file.lastModifiedTime.getEpochSecond.toString
}
