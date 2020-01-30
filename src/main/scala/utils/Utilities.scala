package utils

import java.io.{ FileInputStream, InputStreamReader }
import java.util.Collections

import com.opencsv.CSVReader
import org.apache.commons.lang3.RandomStringUtils
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.Serialization.{ read, write }

import scala.collection.JavaConverters._
import scala.io.Source
import scala.reflect.Manifest

object Utilities {

  private val random = scala.util.Random

  def randomNumberBetweenRange(start: Int, end: Int) = start + random.nextInt((end - start) + 1)

  def randomFromSeq(seq: Seq[_]) = seq(randomNumberBetweenRange(0, seq.size - 1))

  def randomFromArray(seq: Array[_]) = seq(randomNumberBetweenRange(0, seq.size - 1))

  def randomMobileNumber() = randomNumberBetweenRange(7, 9).toString + RandomStringUtils.randomNumeric(9).toString

  def openStream(resource: String) = {
    val file = new java.io.File(resource.trim)
    if (file.exists) {
      new FileInputStream(file)
    } else {
      val list = Collections.list(Thread.currentThread.getContextClassLoader.getResources(resource.trim));
      if (list.isEmpty) {
        null
      } else {
        list.get(0).openStream()
      }
    }
  }

  def readFile(file: String) = Source.fromInputStream(openStream(file)) mkString

  implicit val formats = DefaultFormats

  def serializeJson[A](json: String)(implicit mf: Manifest[A]) = read[A](json)

  def deserializeJson(json: Any) = JsonMethods.mapper.writeValueAsString(Extraction.decompose(json)(formats))

  def readCSV(file: String) = {
    val stream = openStream(file)
    if (stream == null) {
      List.empty
    } else {
      new CSVReader(new InputStreamReader(stream)).readAll.asScala.toList
    }
  }

}
