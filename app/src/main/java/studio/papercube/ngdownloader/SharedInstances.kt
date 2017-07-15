package studio.papercube.ngdownloader

import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.Executors

val sharedOkHttpClient by lazy {
    OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
//        .proxySelector(ProxySelector.)
//            .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 1080)))
            .build()!!
}

val sharedExecutor = Executors.newCachedThreadPool()