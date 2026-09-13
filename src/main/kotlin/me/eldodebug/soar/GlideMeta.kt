package me.eldodebug.soar

object GlideMeta {

    const val CLIENT_NAME: String = "Glide"
    const val VERSION_NUMBER: String = "7.3"
    const val VERSION_IDENTIFIER: Int = 7000
    const val API: String = "https://glideclient.github.io"
    const val SITE: String = "https://glideclient.com"
    const val DISCORD_SERVER_MEMBER_COUNT_API: String = "https://discord.com/api/v9/invites/42PXqKvwxq?with_counts=true"
    @JvmField val BUILD_TYPE: Type = Type.DEV


    enum class Type {
        DEV("Development"), ALPHA("Alpha"), BETA("Beta"), RELEASE("Release"), SPECIAL("Special");


        @JvmField var kind: String

        constructor(kind: String) {
            this.kind = kind
        }
    }
}
