package asyncio

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AbstractEventLoop {

    fun runUntilComplete(function: suspend () -> Unit) = GlobalScope.launch {
        function()
    }

    fun close() {
        //TODO("Not yet implemented")
    }
}