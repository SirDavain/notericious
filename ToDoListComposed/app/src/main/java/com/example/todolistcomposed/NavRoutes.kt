package com.example.todolistcomposed

object NavRoutes {
    const val TODO_LIST_SCREEN = "todoListScreen"
    const val MAIN_SCREEN = "mainScreen"
    const val NOTES_WRITING_SCREEN_ROUTE = "notesWritingScreen"
    const val NOTES_TITLE_ARG = "noteTitle"

    // Use query param style
    const val NOTES_WRITING_SCREEN = "$NOTES_WRITING_SCREEN_ROUTE?$NOTES_TITLE_ARG={$NOTES_TITLE_ARG}"

    // URL encode the title so it can contain special characters like '/'
    fun notesWritingScreenWithOptionalTitle(title: String?): String {
        return if (title != null) {
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            "$NOTES_WRITING_SCREEN_ROUTE?$NOTES_TITLE_ARG=$encodedTitle"
        } else {
            NOTES_WRITING_SCREEN_ROUTE // Navigate without the argument if title is null
        }
    }
}