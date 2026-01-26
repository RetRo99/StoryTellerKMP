package retro99.network.api

interface BaseUrlProvider {
    fun getBaseUrl(): String?

    fun setBaseUrl(url: String)

    fun clearBaseUrl()
}

