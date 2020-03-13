package sample.utils

import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.security.keyvault.secrets.{SecretClient, SecretClientBuilder}

class KeyVaultManager(vaultUrl: String = "https://pierre-spark-key-vault.vault.azure.net/") {

  /**
    * Don't forget to set environment variables AZURE_CLIENT_ID, AZURE_CLIENT_SECRET and AZURE_TENANT_ID
    */
  var client: SecretClient = new SecretClientBuilder()
    .vaultUrl(vaultUrl)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient()

  def getValueFromKey(key: String): String = {
    val testSecret = client.getSecret(key)

    println(s"Secret ${testSecret.getName} retrieved with value ${testSecret.getValue}")

    testSecret.getValue
  }

}
