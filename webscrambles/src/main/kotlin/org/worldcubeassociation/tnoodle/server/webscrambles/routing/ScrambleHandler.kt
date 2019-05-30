package org.worldcubeassociation.tnoodle.server.webscrambles.routing

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.util.toMap
import org.worldcubeassociation.tnoodle.server.RouteHandler
import org.worldcubeassociation.tnoodle.server.webscrambles.InvalidScrambleRequestException
import org.worldcubeassociation.tnoodle.server.webscrambles.ScrambleRequest

import java.util.Date

object ScrambleHandler : RouteHandler {
    override fun install(router: Routing) {
        router.get("/scramble/{filename}") {
            val filename = call.parameters["filename"]!!

            val generationDate = Date()

            val query = call.request.queryParameters.toMap().mapValues { it.value.first() }.toMutableMap()

            // TODO - this means you can't have a round named "seed" or "showIndices" or "callback" or "generationUrl"!
            val seed = query.remove("seed")
            val generationUrl = query.remove("generationUrl")
            val showIndices = query.remove("showIndices") != null

            query.remove("callback")

            val (title, extension) = filename.split(".", limit = 2)

            if (extension.isEmpty()) {
                throw InvalidScrambleRequestException("No extension specified")
            }

            // Note that we parse the scramble requests *after* checking the extension.
            // This way, someone who makes a request for "/scramble/foo.bar" will get a warning about
            // the ".bar" extension, rather than incorrect scramble requests.
            // FIXME do what the comment says
            val scrambleRequests = ScrambleRequest.parseScrambleRequests(query, seed)

            when (extension) {
                "txt" -> {
                    val sb = StringBuilder()
                    for (scrambleRequest in scrambleRequests) {
                        for (j in 0 until scrambleRequest.copies) {
                            for (i in scrambleRequest.scrambles.indices) {
                                val scramble = scrambleRequest.scrambles[i]

                                if (showIndices) {
                                    sb.append(i + 1).append(". ")
                                }

                                // We replace newlines with spaces
                                sb.append(scramble.replace("\n".toRegex(), " "))
                                sb.append("\r\n")
                            }
                        }
                    }

                    call.respondText(sb.toString())
                }
                "json" -> call.respond(scrambleRequests)
                "pdf" -> {
                    val totalPdfOutput = ScrambleRequest.requestsToPdf(title, generationDate, scrambleRequests, null)
                    call.response.header("Content-Disposition", "inline")

                    // Workaround for Chrome bug with saving PDFs:
                    //  https://bugs.chromium.org/p/chromium/issues/detail?id=69677#c35
                    call.response.header("Cache-Control", "public")

                    call.respondBytes(totalPdfOutput.toByteArray(), ContentType.Application.Pdf)
                }
                "zip" -> {
                    val baosZip = ScrambleRequest.requestsToZip(title, generationDate, scrambleRequests, seed, generationUrl, null)

                    call.respondBytes(baosZip.toByteArray(), ContentType.Application.Zip)
                }
                else -> throw InvalidScrambleRequestException("Invalid extension: $extension")
            }
        }
    }
}