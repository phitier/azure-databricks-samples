package sample

import com.microsoft.azure.sqldb.spark.config.Config
import com.microsoft.azure.sqldb.spark.connect._
import org.apache.spark.sql.{SaveMode, SparkSession}
import sample.model.Note

/**
  * Il faut déclarer le DB_URL, DB_USER et DB_PASSWORD en variables d'environnement
  */
object SqlServerUtil {

  val dbName: String = System.getenv("DB_NAME")
  val dbUser: String = System.getenv("DB_USER")
  val dbPassword: String = System.getenv("DB_PASSWORD")

  val writeConfig: Config = Config(Map(
    "url" -> s"$dbName.database.windows.net",
    "databaseName" -> "notes-db",
    "dbTable" -> "dbo.note",
    "user" -> s"$dbUser@$dbName",
    "password" -> dbPassword,
    "connectTimeout" -> "5",
    "queryTimeout" -> "5"
  ))

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("SQL Server Reader")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val sqlContext = spark.sqlContext

    val df = sqlContext.read.sqlDB(writeConfig)
      .withColumnRenamed("note_id", "noteId")
      .withColumnRenamed("note_title", "noteTitle")
      .withColumnRenamed("note_content", "noteContent")

    val ds = df.as[Note]
    ds.show

    val newNotesDs = spark.createDataset(Seq(Note(0, "Titre 3", "Content 3")))

    val finalNotesDs = ds.union(newNotesDs)
    finalNotesDs.show

    finalNotesDs.toDF()
      .withColumnRenamed("noteId", "note_id")
      .withColumnRenamed("noteTitle", "note_title")
      .withColumnRenamed("noteContent", "note_content")
      .write
      .mode(SaveMode.Append)
      .sqlDB(writeConfig)

    // DO NOT CALL when running on Databricks
    spark.close()
  }
}
