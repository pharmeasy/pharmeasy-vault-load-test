package utils

import java.util.Properties

object ConfigManager {

  private final val props: Properties = {
    val props = new Properties
    props.load(Thread.currentThread.getContextClassLoader.getResourceAsStream("config.properties"))
    props
  }

  def getString(key: String, defaultValue: String = ""): String = System.getProperty(key, props.getProperty(key, defaultValue)).trim

  def getInt(key: String, defaultValue: Int = 0): Int = getString(key, defaultValue.toString).toInt

  def getLong(key: String, defaultValue: Long = 0): Long = getString(key, defaultValue.toString).toLong

  def getFloat(key: String, defaultValue: Float = 0): Float = getString(key, defaultValue.toString).toFloat

  def getDouble(key: String, defaultValue: Double = 0): Double = getString(key, defaultValue.toString).toDouble

  def getArray(key: String, delimiter: String = ","): Array[String] = getString(key) split (delimiter) map { e => e.trim } toArray

  def getSeq(key: String, delimiter: String = ","): Seq[String] = getArray(key, delimiter) toSeq
}
