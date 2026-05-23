package com.example.notericious

object NavRoutes {
    const val MAIN_SCREEN = "mainScreen"
    
    const val TODO_LIST_SCREEN_ROUTE = "todoListScreen"
    const val NOTES_WRITING_SCREEN_ROUTE = "notesWritingScreen"

    const val NOTES_TITLE_ARG = "noteTitle"
    const val NOTES_ID_ARG = "noteId"

    const val NOTES_WRITING_SCREEN = "$NOTES_WRITING_SCREEN_ROUTE?$NOTES_ID_ARG={$NOTES_ID_ARG}&$NOTES_TITLE_ARG={$NOTES_TITLE_ARG}"
    const val TODO_LIST_SCREEN = "$TODO_LIST_SCREEN_ROUTE?$NOTES_ID_ARG={$NOTES_ID_ARG}&$NOTES_TITLE_ARG={$NOTES_TITLE_ARG}"

    // URL encode the title so it can contain special characters like '/'
    fun notesWritingScreenWithOptionalTitle(title: String?, noteId: Int? = null): String {
        return buildRoute(NOTES_WRITING_SCREEN_ROUTE, title, noteId)
    }

    fun todoListScreenWithOptionalTitle(title: String?, listId: Int? = null): String {
        return buildRoute(TODO_LIST_SCREEN_ROUTE, title, listId)
    }

    private fun buildRoute(route: String, title: String?, id: Int?): String {
        val base = "$route?"
        val idPart = if (id != null) "$NOTES_ID_ARG=$id" else ""
        val titlePart = if (title != null) {
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            "$NOTES_TITLE_ARG=$encodedTitle"
        } else ""

        return if (idPart.isNotEmpty() && titlePart.isNotEmpty()) {
            "$base$idPart&$titlePart"
        } else if (idPart.isNotEmpty()) {
            "$base$idPart"
        } else if (titlePart.isNotEmpty()) {
            "$base$titlePart"
        } else {
            route
        }
    }
}