package name.abhijitsarkar.scala.scauth.model

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import TwitterJsonSupport._
import scala.io.Source

class TwitterJsonSupportSpec extends FlatSpec with Matchers {
  it should "parse a tweet with no more fields than those we're interested in" in {
    val userJson = """{
        |"name": "Test User",
        |"screen_name": "testUser" ,
        |"location": "Somewhere",
        |"followers_count": 1
    |}""".stripMargin.replaceAll("\n", " ")

    val tweetJson = s"""{
        |"text": "Selfie",
        |"created_at": "Sat Oct 03 08:11:19 +0000 2015" ,
        |"user": $userJson
    |}""".stripMargin.replaceAll("\n", " ")

    val tweet = parseTweet(tweetJson)

    tweet.text should contain ("Selfie")
    tweet.author.screenName shouldBe ("testUser")
    tweet.createdAt.getDayOfMonth shouldBe (3)
  }
  
  it should "parse a tweet with no text (retweet)" in {
    val userJson = """{
        |"name": "Test User",
        |"screen_name": "testUser" ,
        |"location": "Somewhere",
        |"followers_count": 1
    |}""".stripMargin.replaceAll("\n", " ")

    val tweetJson = s"""{
        |"created_at": "Sat Oct 03 08:11:19 +0000 2015" ,
        |"user": $userJson
    |}""".stripMargin.replaceAll("\n", " ")

    val tweet = parseTweet(tweetJson)

    tweet.text shouldBe empty
    tweet.author.screenName shouldBe ("testUser")
    tweet.createdAt.getDayOfMonth shouldBe (3)
  }

  it should "parse a tweet with more fields than those we're interested in" in {
    val is = getClass().getResourceAsStream("/tweet1.json")

    val tweetJson = Source.fromInputStream(is).mkString.stripMargin.replaceAll("\n", " ")
    
    val tweet = parseTweet(tweetJson)

    tweet.text should contain ("blah")
    tweet.author.screenName shouldBe ("Fan_namo001")
    tweet.createdAt.getDayOfMonth shouldBe (3)
  }
}