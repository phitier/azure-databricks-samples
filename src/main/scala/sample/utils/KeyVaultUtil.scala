package sample.utils

object KeyVaultUtil {

  def main(args: Array[String]): Unit = {

    val keyVaultManager = new KeyVaultManager()
    val secret = keyVaultManager.getValueFromKey("testSecret")
    println(s"$secret")
  }

}
