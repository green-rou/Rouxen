package com.greenrou.rouxen.feature.traffic.data

import java.util.concurrent.ConcurrentHashMap

class RateAggregator {

    private data class Sample(val timeMs: Long, val rxBytes: Long, val txBytes: Long)

    private val samples = ConcurrentHashMap<Int, ArrayDeque<Sample>>()

    fun record(uid: Int, rxBytes: Long, txBytes: Long, now: Long) {
        val deque = samples.getOrPut(uid) { ArrayDeque() }
        synchronized(deque) {
            deque.addLast(Sample(now, rxBytes, txBytes))
            while (deque.size > WINDOW_SIZE) deque.removeFirst()
        }
    }

    fun rateBps(uid: Int): Pair<Long, Long> {
        val deque = samples[uid] ?: return 0L to 0L
        synchronized(deque) {
            if (deque.size < 2) return 0L to 0L
            val first = deque.first()
            val last = deque.last()
            val elapsedMs = last.timeMs - first.timeMs
            if (elapsedMs <= 0) return 0L to 0L
            val rxRate = (last.rxBytes - first.rxBytes) * 1000 / elapsedMs
            val txRate = (last.txBytes - first.txBytes) * 1000 / elapsedMs
            return rxRate to txRate
        }
    }

    fun prune(activeUids: Set<Int>) {
        samples.keys.retainAll(activeUids)
    }

    companion object {
        private const val WINDOW_SIZE = 10
    }
}
