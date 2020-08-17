package com.packpng.worldquery.monitor

import java.util.Observable

import com.packpng.worldquery.monitor.WorldAreaListener._
import org.bukkit.block.Block
import org.bukkit.event.block.{BlockBreakEvent, BlockPlaceEvent, LeavesDecayEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.util.{BoundingBox, Vector => BVector}
import org.bukkit.{Location, World}

import scala.collection.mutable

class WorldAreaListener(
  world: World,
  boundingBox: BoundingBox,
  materials: Set[String]
) extends Observable with Listener {

  def getBlocks: Map[BlockLocation, String] = blocks.toMap // immutable

  // location -> material
  private lazy val blocks: mutable.Map[BlockLocation, String] = {
    val blockTuples = (for {
      x <- boundingBox.getMinX.toInt to boundingBox.getMaxX.toInt
      y <- boundingBox.getMinY.toInt to boundingBox.getMaxY.toInt
      z <- boundingBox.getMinZ.toInt to boundingBox.getMaxZ.toInt
    } yield world.getBlockAt(x, y, z) match {
      case block: Block if isBlockOfInterest(block) => Some(block)
      case _ => None
    }).flatten.map(block =>
      (BlockLocation(block.getLocation), getMaterial(block))
    )

    collection.mutable.Map(blockTuples: _*)
  }

  private def isBlockOfInterest(block: Block): Boolean = materials contains getMaterial(block)

  @EventHandler(priority=EventPriority.MONITOR)
  def onBlockPlaceEvent(blockEvent: BlockPlaceEvent): Unit = {
    if (boundingBox.contains(blockEvent.getBlock.getBoundingBox)) {
      val placedMaterial: String = getMaterial(blockEvent.getBlockPlaced)

      if (materials contains placedMaterial) {
        updateBlock(BlockLocation(blockEvent.getBlock.getLocation), Some(placedMaterial))
      }
    }
  }

  @EventHandler(priority=EventPriority.MONITOR)
  def onBlockBreakEvent(blockEvent: BlockBreakEvent): Unit = {
    val location = BlockLocation(blockEvent.getBlock.getLocation())
    updateBlock(location, None)
  }

  @EventHandler(priority=EventPriority.MONITOR)
  def onBlockDecayEvent(blockEvent: LeavesDecayEvent): Unit = {
    val location = BlockLocation(blockEvent.getBlock.getLocation())
    updateBlock(location, None)
  }

  private def updateBlock(location: BlockLocation, material: Option[String]): Unit = {
    val changed = material match {
      case Some(material) =>
        blocks(location) = material
        true
      case None => blocks.remove(location).isDefined
    }

    if (changed) {
      setChanged()
      notifyObservers()
    }
  }
}

object WorldAreaListener {

  protected case class BlockLocation(
    x: Int,
    y: Int,
    z: Int
  ) {
    def toVector: BVector = new BVector(x, y, z)
  }
  private object BlockLocation {
    def apply(location: Location): BlockLocation = BlockLocation(
      x = location.getBlockX,
      y = location.getBlockY,
      z = location.getBlockZ
    )
  }

  private def getMaterial(block: Block): String = block.getType.name
}
