package retro99.games.api

class QueryParamsScope {
    val params = mutableMapOf<String, Any?>()

    infix fun String.to(value: String?) {
        if (value != null) {
            params[this] = value
        }
    }

    infix fun String.to(value: Int?) {
        if (value != null) {
            params[this] = value
        }
    }

    infix fun String.to(value: Long?) {
        if (value != null) {
            params[this] = value
        }
    }

    infix fun String.to(value: Double?) {
        if (value != null) {
            params[this] = value
        }
    }

    infix fun String.to(value: Boolean?) {
        if (value != null) {
            params[this] = value
        }
    }

    infix fun String.toStrList(value: List<String?>?) {
        if (value != null) {
            params[this] = value.filterNotNull()
        }
    }

    infix fun String.toIntList(value: List<Int?>?) {
        if (value != null) {
            params[this] = value.filterNotNull()
        }
    }
}