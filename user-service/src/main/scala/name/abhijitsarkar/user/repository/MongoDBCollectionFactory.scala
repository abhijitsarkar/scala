package name.abhijitsarkar.user.repository

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import name.abhijitsarkar.user.domain.UserAttributes._
import org.slf4j.LoggerFactory

object MongoDBCollectionFactory {
  private val logger = LoggerFactory.getLogger(getClass)
  
  def newCollection(name: String) = {
    val collection = MongoClient()("akka")(name)

    collection.createIndex(MongoDBObject(PHONE_NUM.toString -> 1), MongoDBObject("unique" -> true))
    collection.createIndex(MongoDBObject(EMAIL.toString -> 1), MongoDBObject("unique" -> true, "sparse" -> true))
    
    collection.indexInfo.foreach { index => logger.debug(s"Index: ${index.toMap}") }

    collection
  }
}