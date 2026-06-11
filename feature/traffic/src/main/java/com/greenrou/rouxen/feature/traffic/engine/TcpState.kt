package com.greenrou.rouxen.feature.traffic.engine

enum class TcpState {
    SYN_RECEIVED,

    ESTABLISHED,

    FIN_WAIT_1,

    FIN_WAIT_2,

    CLOSE_WAIT,

    LAST_ACK,

    TIME_WAIT,

    CLOSED,
}
