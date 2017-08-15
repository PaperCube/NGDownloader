package studio.papercube.ngdownloader

open class UniqueInstanceManager<T> {
    private val uniqueInstanceMap: MutableMap<Class<*>, T> = HashMap()

    inline fun <reified InstanceClass : T> get(): T {
        return get(InstanceClass::class.java)
    }

    fun get(jClass: Class<out T>): T {
        return uniqueInstanceMap[jClass] ?: let {
            val instance = jClass.getConstructor().newInstance()
            uniqueInstanceMap.put(jClass, instance)
            instance
        }
    }
}