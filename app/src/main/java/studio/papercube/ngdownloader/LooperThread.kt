package studio.papercube.ngdownloader

open class LooperThread(
        private val interval: Long,
        name: String? = null,
        private val task: LooperThread.() -> Boolean) : Thread() {

    companion object {
        @JvmStatic @Volatile private var counter: Int = 0
    }

    init {
        if (name == null) {
            setName(name)
        } else setName("LooperThread-${++counter}")
    }

    override fun run() {
        try {
            while (true) {
                if(!task()) break
                Thread.sleep(interval)
            }
        } catch (e: InterruptedException) {
            return
        }
    }
}