package sample

import com.databricks.dbutils_v1.DBUtilsHolder.dbutils
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
  * Il faut déclarer le CLIENT_ID, CLIENT_SECRET et TENANT_ID (ID de répertoire du keyvault)
  */
object ReadFromAzureDriver {

  def main(args: Array[String]): Unit = {

    val config = new SparkConf
    config.setMaster("local[*]")
    config.setAppName("app")

    config.set("spark.databricks.driver.disableScalaOutput", "true")

    val datalakeName = "pierredatalake"
    val tenantId = System.getenv("TENANT_ID")

    config.set("fs.azure.skipUserGroupMetadataDuringInitialization", "true")
    config.set(s"fs.azure.account.auth.type.$datalakeName.dfs.core.windows.net", "OAuth")
    config.set(s"fs.azure.account.oauth.provider.type.$datalakeName.dfs.core.windows.net", "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider")
    config.set(s"fs.azure.account.oauth2.client.id.$datalakeName.dfs.core.windows.net", System.getenv("DATALAKE_CLIENT_ID"))
    config.set(s"fs.azure.account.oauth2.client.secret.$datalakeName.dfs.core.windows.net", System.getenv("DATALAKE_CLIENT_SECRET"))
    config.set(s"fs.azure.account.oauth2.client.endpoint.$datalakeName.dfs.core.windows.net", s"https://login.microsoftonline.com/${tenantId}/oauth2/token")

    val spark = SparkSession.builder()
      .config(config)
      .getOrCreate()

    dbutils.fs.ls("/").foreach(fileInfo => println(fileInfo.name))

    val df = spark.read
      .option("header", "true")
      .csv(s"abfss://test-spark-container@$datalakeName.dfs.core.windows.net/On_Time_Reporting_Carrier_On_Time_Performance_(1987_present)_2019_1.csv")

    df.show(10)

    // do NOT call SparkSession#close on Azure Databricks
    //spark.close
  }
}
